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
    private static final String COMMUNITY_TYPE = "community";
    private static final String CARD_TYPE = "card";
    private static final String NOTICE_TYPE = "notice";


    public Flux<Object> getAllBookmarks(String userId) {
        Flux<String> cardIds = bookmarkRepository.findAllByUserIdAndPostType(userId, CARD_TYPE)
                .map(Bookmark::getPostId)
                .distinct();
        Flux<Card> bookmarkedCards = cardIds.flatMap(cardRepository::findById);

        Flux<String> communityIds = bookmarkRepository.findAllByUserIdAndPostType(userId, COMMUNITY_TYPE)
                .map(Bookmark::getPostId)
                .distinct();
        Flux<CommunityPost> bookmarkedCommunities = communityIds.flatMap(communityPostRepository::findById);

        return Flux.merge(
                bookmarkedCards.cast(Object.class),
                bookmarkedCommunities.cast(Object.class)
        );
    }

    public Flux<Object> getAllLikes(String userId) {
        Flux<String> noticeIds = likeRepository.findAllByUserIdAndPostType(userId, NOTICE_TYPE)
                .map(Like::getPostId)
                .distinct();
        Flux<Notice> likedNotices = noticeIds.flatMap(noticeRepository::findById);

        Flux<String> communityIds = likeRepository.findAllByUserIdAndPostType(userId, COMMUNITY_TYPE)
                .map(Like::getPostId)
                .distinct();
        Flux<CommunityPost> likedCommunities = communityIds.flatMap(communityPostRepository::findById);

        return Flux.merge(
                likedNotices.cast(Object.class),
                likedCommunities.cast(Object.class)
        );
    }



    //내가 쓴 글 조회
    public Flux<CommunityResponseDto> getMyCommunityPosts(String userId) {
        return communityPostRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
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
