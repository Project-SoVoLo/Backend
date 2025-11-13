package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.*;
import soboro.soboro_web.dto.CommunityRequestDto;
import soboro.soboro_web.dto.CommunityResponseDto;
import soboro.soboro_web.repository.BookmarkRepository;
import soboro.soboro_web.repository.CommunityPostRepository;
import soboro.soboro_web.repository.LikeRepository;
import soboro.soboro_web.repository.UserRepository;
import soboro.soboro_web.dto.CommentDto;
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

    private static final String POST_TYPE = "community";

    public Flux<CommunityResponseDto> getAll(String loginEmailOrNull) {
        if (loginEmailOrNull == null || loginEmailOrNull.isEmpty()) {
            return postRepo.findAllByOrderByCreatedAtDesc()
                    .map(post -> toResponse(post, false, false));
        }
        // email → userId 1회 매핑 후, viewer 상태 계산
        return resolveUserId(loginEmailOrNull)
                .flatMapMany(uid ->
                        postRepo.findAllByOrderByCreatedAtDesc()
                                .flatMap(post ->
                                        likeRepo.findByUserIdAndPostIdAndPostType(uid, post.getId(), POST_TYPE).hasElement()
                                                .flatMap(liked ->
                                                        bookmarkRepo.findByUserIdAndPostIdAndPostType(uid, post.getId(), POST_TYPE).hasElement()
                                                                .map(bookmarked -> toResponse(post, liked, bookmarked))
                                                )
                                )
                );
    }

    public Mono<CommunityResponseDto> getById(String postId, String loginEmailOrNull) {
        if (loginEmailOrNull == null || loginEmailOrNull.isEmpty()) {
            return postRepo.findById(postId)
                    .map(post -> toResponse(post, false, false));
        }
        return resolveUserId(loginEmailOrNull)
                .flatMap(uid -> postRepo.findById(postId)
                        .flatMap(post ->
                                likeRepo.findByUserIdAndPostIdAndPostType(uid, postId, POST_TYPE).hasElement()
                                        .flatMap(liked ->
                                                bookmarkRepo.findByUserIdAndPostIdAndPostType(uid, postId, POST_TYPE).hasElement()
                                                        .map(bookmarked -> toResponse(post, liked, bookmarked))
                                        )));
    }

    public Mono<CommunityResponseDto> create(CommunityRequestDto req, String loginEmail) {
        return loadUser(loginEmail)
                .flatMap(u -> {
                    CommunityPost p = new CommunityPost();
                    p.setTitle(req.getTitle());
                    p.setBlocks(req.getBlocks());
                    p.setUserId(u.getUserId());        //DB에는 userId 저장
                    p.setNickname(u.getNickname());
                    p.setLikeCount(0);
                    p.setBookmarkCount(0);
                    p.setCommentCount(0);
                    p.setPostType(POST_TYPE);

                    Instant now = Instant.now();
                    p.setCreatedAt(now);
                    p.setUpdatedAt(now);
                    return postRepo.save(p);
                })
                .map(saved -> toResponse(saved, false, false));
    }

    public Mono<CommunityResponseDto> update(String id, CommunityRequestDto req, String loginEmail) {
        return loadUser(loginEmail)
                .flatMap(u -> postRepo.findById(id)
                        .flatMap(existing -> {
                            boolean owner = u.getUserId().equals(existing.getUserId());
                            if (!owner) return Mono.error(new IllegalAccessException("수정 권한이 없습니다."));
                            existing.setTitle(req.getTitle());
                            existing.setBlocks(req.getBlocks());
                            existing.setUpdatedAt(Instant.now());
                            return postRepo.save(existing);
                        }))
                .map(saved -> toResponse(saved, false, false));
    }

    //삭제
    public Mono<Void> delete(String id, String loginEmail, boolean isAdmin) {
        if (isAdmin) {
            return postRepo.findById(id)
                    .flatMap(existing -> Mono.when(
                            likeRepo.deleteByPostIdAndPostType(id, POST_TYPE),
                            bookmarkRepo.deleteByPostIdAndPostType(id, POST_TYPE),
                            commentRepo.deleteByPostId(id)
                    ).then(postRepo.delete(existing)));
        }

        return loadUser(loginEmail)
                .flatMap(u -> postRepo.findById(id)
                        .flatMap(existing -> {
                            boolean owner = u.getUserId().equals(existing.getUserId());
                            if (!owner) return Mono.error(new IllegalAccessException("삭제 권한이 없습니다."));
                            return Mono.when(
                                    likeRepo.deleteByPostIdAndPostType(id, POST_TYPE),
                                    bookmarkRepo.deleteByPostIdAndPostType(id, POST_TYPE),
                                    commentRepo.deleteByPostId(id)
                            ).then(postRepo.delete(existing));
                        }));
    }

    // 좋아요/북마크
    public Mono<Boolean> toggleLikeByEmail(String loginEmail, String postId) {
        return resolveUserId(loginEmail).flatMap(uid -> toggleLike(uid, postId));
    }

    public Mono<Boolean> toggleBookmarkByEmail(String loginEmail, String postId) {
        return resolveUserId(loginEmail).flatMap(uid -> toggleBookmark(uid, postId));
    }

    // 기존 userId 버전
    public Mono<Boolean> toggleLike(String userId, String postId) {
        return likeRepo.findByUserIdAndPostIdAndPostType(userId, postId, POST_TYPE)
                .flatMap(ex -> likeRepo.deleteByUserIdAndPostIdAndPostType(userId, postId, POST_TYPE)
                        .then(refreshLikeCount(postId))
                        .thenReturn(false))
                .switchIfEmpty(
                        likeRepo.save(newLike(userId, postId))
                                .then(refreshLikeCount(postId))
                                .thenReturn(true)
                );
    }

    public Mono<Boolean> toggleBookmark(String userId, String postId) {
        return bookmarkRepo.findByUserIdAndPostIdAndPostType(userId, postId, POST_TYPE)
                .flatMap(ex -> bookmarkRepo.deleteByUserIdAndPostIdAndPostType(userId, postId, POST_TYPE)
                        .then(refreshBookmarkCount(postId))
                        .thenReturn(false))
                .switchIfEmpty(
                        bookmarkRepo.save(newBookmark(userId, postId))
                                .then(refreshBookmarkCount(postId))
                                .thenReturn(true)
                );
    }

    //댓글
    public Flux<CommentDto.Item> listComments(String postId) {
        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId).map(this::toCommentItem);
    }

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
                .flatMap(saved -> refreshCommentCount(postId).thenReturn(toCommentItem(saved)));
    }

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

    public Mono<CommentDto.MessageRes> deleteComment(String commentId, String loginEmail, boolean isAdmin) {
        if (isAdmin) {
            return commentRepo.findById(commentId)
                    .flatMap(c -> commentRepo.delete(c)
                            .then(refreshCommentCount(c.getPostId()))
                            .thenReturn(new CommentDto.MessageRes("deleted")));
        }
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

    // helpers
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
        b.setPostType(POST_TYPE);
        return b;
    }

    private Like newLike(String userId, String postId) {
        Like l = new Like();
        l.setUserId(userId);
        l.setPostId(postId);
        l.setPostType(POST_TYPE);
        return l;
    }

    private Mono<CommunityPost> refreshBookmarkCount(String postId) {
        return bookmarkRepo.countByPostIdAndPostType(postId, POST_TYPE)
                .flatMap(cnt -> postRepo.findById(postId)
                        .flatMap(p -> {
                            p.setBookmarkCount(cnt);
                            p.setUpdatedAt(Instant.now());
                            return postRepo.save(p);
                        }).switchIfEmpty(Mono.empty()));
    }

    private Mono<CommunityPost> refreshLikeCount(String postId) {
        return likeRepo.countByPostIdAndPostType(postId, POST_TYPE)
                .flatMap(cnt -> postRepo.findById(postId)
                        .flatMap(p -> {
                            p.setLikeCount(cnt);
                            p.setUpdatedAt(Instant.now());
                            return postRepo.save(p);
                        }).switchIfEmpty(Mono.empty()));
    }

    private CommentDto.Item toCommentItem(Comment c) {
        return new CommentDto.Item(
                c.getCommentId(), c.getPostId(), c.getNickname(),
                c.getContent(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    private Mono<CommunityPost> refreshCommentCount(String postId) {
        return commentRepo.countByPostId(postId)
                .flatMap(cnt -> postRepo.findById(postId)
                        .flatMap(p -> {
                            p.setCommentCount(cnt);
                            p.setUpdatedAt(Instant.now());
                            return postRepo.save(p);
                        }));
    }

    // email → User / userId helpers
    private Mono<User> loadUser(String email) {
        return userRepo.findByUserEmail(email)
                .switchIfEmpty(Mono.error(new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.")));
    }

    private Mono<String> resolveUserId(String email) {
        return loadUser(email).map(User::getUserId);
    }
}

