package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;
import soboro.soboro_web.domain.CommunityPost;
import soboro.soboro_web.domain.Like;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.dto.CommunityRequestDto;
import soboro.soboro_web.dto.CommunityResponseDto;
import soboro.soboro_web.repository.BookmarkRepository;
import soboro.soboro_web.repository.CommunityPostRepository;
import soboro.soboro_web.repository.LikeRepository;
import soboro.soboro_web.repository.UserRepository;
import soboro.soboro_web.dto.CommentDto;
import soboro.soboro_web.domain.Comment;
import soboro.soboro_web.repository.CommentRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository postRepo;
    private final UserRepository userRepo;

    private final BookmarkRepository bookmarkRepo;
    private final LikeRepository likeRepo;
    private final CommentRepository commentRepo;

    // 목록(최신순)
    public Flux<CommunityResponseDto> getAll() {
        return postRepo.findAllByOrderByCreatedAtDesc()
                .map(p -> toResponse(p, false, false)); // viewer 상태가 필요하면 호출부에서 계산해 넘겨줘도 됨
    }

    public Mono<CommunityResponseDto> getById(String id) {
        return postRepo.findById(id)
                .map(p -> toResponse(p, false, false));
    }

    // 생성
    public Mono<CommunityResponseDto> create(CommunityRequestDto req, String loginEmail) {
        return loadUser(loginEmail)
                .flatMap(u -> {
                    CommunityPost p = new CommunityPost();
                    p.setTitle(req.getTitle());
                    p.setBlocks(req.getBlocks());
                    p.setUserId(u.getUserId());
                    p.setNickname(u.getNickname());
                    p.setLikeCount(0);
                    p.setBookmarkCount(0);
                    p.setCommentCount(0);

                    Instant now = Instant.now();
                    p.setCreatedAt(now);   // 감사 비활성 → 수동 세팅
                    p.setUpdatedAt(now);

                    return postRepo.save(p);
                })
                .map(saved -> toResponse(saved, false, false));
    }

    // 수정
    public Mono<CommunityResponseDto> update(String id, CommunityRequestDto req, String loginEmail) {
        return loadUser(loginEmail)
                .flatMap(u -> postRepo.findById(id)
                        .flatMap(existing -> {
                            boolean owner = existing.getUserId() != null
                                    && existing.getUserId().equals(u.getUserId());
                            if (!owner) {
                                return Mono.error(new IllegalAccessException("수정 권한이 없습니다."));
                            }
                            existing.setTitle(req.getTitle());
                            existing.setBlocks(req.getBlocks());
                            existing.setUpdatedAt(Instant.now());
                            return postRepo.save(existing);
                        }))
                .map(saved -> toResponse(saved, false, false));
    }
    //삭제
    public Mono<Void> delete(String id, String loginEmail, boolean isAdmin) {
        //관리자인 경우 바로 삭제
        if (isAdmin) {
            return postRepo.findById(id)
                    .flatMap(existing -> {
                        Mono<Void> delLikes = likeRepo.deleteByPostId(id);
                        Mono<Void> delBookmarks = bookmarkRepo.deleteByPostId(id);
                        Mono<Void> delComments = commentRepo.deleteByPostId(id);
                        return Mono.when(delLikes, delBookmarks, delComments)
                                .then(postRepo.delete(existing));
                    });
            // .switchIfEmpty(Mono.error(new IllegalStateException("게시글을 찾을 수 없습니다."))); // 필요 시 404 처리
        }

        //일반 사용자인 경우 확인 후 삭제
        return loadUser(loginEmail)
                .flatMap(u -> postRepo.findById(id)
                        .flatMap(existing -> {
                            boolean owner = existing.getUserId() != null
                                    && existing.getUserId().equals(u.getUserId());
                            if (!owner) {
                                return Mono.error(new IllegalAccessException("삭제 권한이 없습니다."));
                            }

                            Mono<Void> delLikes = likeRepo.deleteByPostId(id);
                            Mono<Void> delBookmarks = bookmarkRepo.deleteByPostId(id);
                            Mono<Void> delComments = commentRepo.deleteByPostId(id);

                            return Mono.when(delLikes, delBookmarks, delComments)
                                    .then(postRepo.delete(existing));
                        }));
        // .switchIfEmpty(Mono.error(new IllegalStateException("게시글을 찾을 수 없습니다."))); // 필요 시 404 처리
    }


    //북마크
    public Mono<Boolean> toggleBookmark(String userId, String postId) {
        return bookmarkRepo.findByUserIdAndPostId(userId, postId)
                .flatMap(ex -> bookmarkRepo.deleteByUserIdAndPostId(userId, postId)
                        .then(refreshBookmarkCount(postId))
                        .thenReturn(false))
                .switchIfEmpty(
                        bookmarkRepo.save(newBookmark(userId, postId))
                                .then(refreshBookmarkCount(postId))
                                .thenReturn(true));
    }

    //좋아요
    public Mono<Boolean> toggleLike(String userId, String postId) {
        return likeRepo.findByUserIdAndPostId(userId, postId)
                .flatMap(ex -> likeRepo.deleteByUserIdAndPostId(userId, postId)
                        .then(refreshLikeCount(postId))
                        .thenReturn(false))
                .switchIfEmpty(
                        likeRepo.save(newLike(userId, postId))
                                .then(refreshLikeCount(postId))
                                .thenReturn(true));
    }

    //댓글
    public Flux<CommentDto.Item> listComments(String postId) {
        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId)
                .map(this::toCommentItem);
    }

    //댓글 작성
    public Mono<CommentDto.Item> addComment(String postId, String loginEmail, CommentDto.CreateReq req) {
        return loadUser(loginEmail)
                .flatMap(u -> {
                    Comment c = new Comment();
                    c.setPostId(postId);
                    c.setUserId(u.getUserId());
                    c.setNickname(u.getNickname());
                    c.setContent(req.content());
                    var now = Instant.now();
                    c.setCreatedAt(now);
                    c.setUpdatedAt(now);
                    return commentRepo.save(c);
                })
                // 댓글 수 동기화
                .flatMap(saved -> refreshCommentCount(postId).thenReturn(toCommentItem(saved)));
    }

    //댓글 수정
    public Mono<CommentDto.Item> updateComment(String commentId, String loginEmail, CommentDto.UpdateReq req) {
        return loadUser(loginEmail)
                .flatMap(u -> commentRepo.findById(commentId)
                        .flatMap(c -> {
                            boolean owner = u.getUserId().equals(c.getUserId());
                            if (!owner) return Mono.error(new IllegalAccessException("수정 권한이 없습니다."));
                            c.setContent(req.content());
                            c.setUpdatedAt(Instant.now());
                            return commentRepo.save(c).map(this::toCommentItem);
                        }));
    }

    //댓글 삭제

    public Mono<CommentDto.MessageRes> deleteComment(String commentId, String loginEmail, boolean isAdmin) {
        //관리자인 경우 바로 삭제
        if (isAdmin) {
            return commentRepo.findById(commentId)
                    .flatMap(c -> commentRepo.delete(c)
                            .then(refreshCommentCount(c.getPostId()))
                            .thenReturn(new CommentDto.MessageRes("deleted")));
        }

        //일반 사용자인 경우 확인 후 삭제
        return loadUser(loginEmail)
                .flatMap(u -> commentRepo.findById(commentId)
                        .flatMap(c -> {
                            boolean owner = u.getUserId().equals(c.getUserId());
                            if (!owner) return Mono.error(new IllegalAccessException("삭제 권한이 없습니다."));
                            return commentRepo.delete(c)
                                    .then(refreshCommentCount(c.getPostId()))
                                    .thenReturn(new CommentDto.MessageRes("deleted"));
                        }));
    }


    //헬퍼
    // Entity -> Response DTO 매핑
    private CommunityResponseDto toResponse(CommunityPost p, boolean likedByMe, boolean bookmarkedByMe) {
        CommunityResponseDto dto = new CommunityResponseDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setNickname(p.getNickname());
        dto.setBlocks(p.getBlocks());
        dto.setLikeCount(p.getLikeCount());
        dto.setBookmarkCount(p.getBookmarkCount());
        dto.setCommentCount(p.getCommentCount());
        dto.setLikedByMe(likedByMe);
        dto.setBookmarkedByMe(bookmarkedByMe);
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    private Bookmark newBookmark(String userId, String postId) {
        Bookmark b = new Bookmark();
        b.setUserId(userId);
        b.setPostId(postId);
        return b;
    }

    private Like newLike(String userId, String postId) {
        Like l = new Like();
        l.setUserId(userId);
        l.setPostId(postId);
        return l;
    }

    // 카운트
    private Mono<CommunityPost> refreshBookmarkCount(String postId) {
        return bookmarkRepo.countByPostId(postId)
                .flatMap(cnt -> postRepo.findById(postId)
                        .flatMap(p -> {
                            p.setBookmarkCount(cnt);
                            p.setUpdatedAt(Instant.now());
                            return postRepo.save(p);
                        }));
    }

    private Mono<CommunityPost> refreshLikeCount(String postId) {
        return likeRepo.countByPostId(postId)
                .flatMap(cnt -> postRepo.findById(postId)
                        .flatMap(p -> {
                            p.setLikeCount(cnt);
                            p.setUpdatedAt(Instant.now());
                            return postRepo.save(p);
                        }));
    }

    private Mono<User> loadUser(String email) {
        return userRepo.findByUserEmail(email)
                .switchIfEmpty(Mono.error(new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.")));
    }
    //댓글
    private CommentDto.Item toCommentItem(Comment c) {
        return new CommentDto.Item(
                c.getCommentId(), c.getPostId(), c.getNickname(),
                c.getContent(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    // 댓글 수 동기화 → CommunityPost.commentCount 갱신
    private Mono<CommunityPost> refreshCommentCount(String postId) {
        return commentRepo.countByPostId(postId)
                .flatMap(cnt -> postRepo.findById(postId)
                        .flatMap(p -> {
                            p.setCommentCount(cnt);
                            p.setUpdatedAt(Instant.now());
                            return postRepo.save(p);
                        }));
    }
}
