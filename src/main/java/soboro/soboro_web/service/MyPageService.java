package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;
import soboro.soboro_web.domain.Card;
import soboro.soboro_web.domain.Like;
import soboro.soboro_web.domain.Notice;
import soboro.soboro_web.domain.CommunityPost;
import soboro.soboro_web.repository.BookmarkRepository;
import soboro.soboro_web.repository.CardRepository;
import soboro.soboro_web.repository.LikeRepository;
import soboro.soboro_web.repository.NoticeRepository;
import soboro.soboro_web.repository.CommunityPostRepository;
import soboro.soboro_web.repository.UserRepository;
import soboro.soboro_web.dto.CommunityResponseDto;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final BookmarkRepository bookmarkRepository;
    private final CardRepository cardRepository;
    private final CommunityPostRepository communityPostRepository; // ✨ 추가
    private final LikeRepository likeRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    /** 내가 북마크한 전체(카드뉴스 + 커뮤니티) */
    public Flux<Object> getAllBookmarks(String userId) {
        // 북마크한 postId들
        Flux<String> bookmarkedPostIds = bookmarkRepository.findAllByUserId(userId)
                .map(Bookmark::getPostId)
                .distinct();

        // 카드뉴스 북마크
        Flux<Card> bookmarkedCards = bookmarkedPostIds.flatMap(cardRepository::findById);

        // 커뮤니티 북마크 ✨
        Flux<CommunityPost> bookmarkedCommunities = bookmarkedPostIds.flatMap(communityPostRepository::findById);

        // 합치기 (서로 타입이 달라서 Object로 캐스팅)
        return Flux.merge(
                bookmarkedCards.cast(Object.class),
                bookmarkedCommunities.cast(Object.class)
        );
    }

    /** 내가 좋아요한 전체(공지사항 + 커뮤니티) */
    public Flux<Object> getAllLikes(String userId) {
        // 좋아요한 postId들
        Flux<String> likedPostIds = likeRepository.findAllByUserId(userId)
                .map(Like::getPostId)
                .distinct();

        // 공지 좋아요
        Flux<Notice> likedNotices = likedPostIds.flatMap(noticeRepository::findById);

        // 커뮤니티 좋아요 ✨
        Flux<CommunityPost> likedCommunities = likedPostIds.flatMap(communityPostRepository::findById);

        return Flux.merge(
                likedNotices.cast(Object.class),
                likedCommunities.cast(Object.class)
        );
    }
    //내가 쓴 글 조회
    public Flux<CommunityResponseDto> getMyCommunityPosts(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
                .switchIfEmpty(Mono.error(new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.")))
                .flatMapMany(u -> communityPostRepository.findAllByUserIdOrderByCreatedAtDesc(u.getUserId()))
                .map(this::toResponse);
    }

    private CommunityResponseDto toResponse(CommunityPost p) {
        CommunityResponseDto dto = new CommunityResponseDto();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setNickname(p.getNickname());
        dto.setBlocks(p.getBlocks());
        dto.setLikeCount(p.getLikeCount());
        dto.setBookmarkCount(p.getBookmarkCount());
        dto.setCommentCount(p.getCommentCount());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        // 작성자 본인 목록이라 likedByMe/bookmarkedByMe 계산은 생략(필요하면 계산해서 세팅)
        return dto;
    }
}
