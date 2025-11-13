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
import soboro.soboro_web.domain.User;


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

public Flux<Object> getAllBookmarks(String userId, String email) {

    //카드뉴스 북마크
    Flux<String> cardIds = bookmarkRepository.findAllByPostType(CARD_TYPE)
            .filter(b -> b.getUserId().equals(userId) || b.getUserId().equals(email))
            .map(Bookmark::getPostId)
            .distinct();

    Flux<Card> bookmarkedCards = cardIds.flatMap(cardRepository::findById);

    //커뮤니티 북마크
    Flux<String> communityIds = bookmarkRepository
            .findAllByUserIdAndPostType(userId, COMMUNITY_TYPE)
            .map(Bookmark::getPostId)
            .distinct();

    Flux<CommunityPost> bookmarkedCommunities =
            communityIds.flatMap(communityPostRepository::findById);

    return Flux.merge(
            bookmarkedCards.cast(Object.class),
            bookmarkedCommunities.cast(Object.class)
    );
}

    public Flux<Object> getAllLikes(String email) {

        //공지사항 좋아요
        Flux<Object> likedNotices = likeRepository.findAllByUserIdAndPostType(email, NOTICE_TYPE)
                .map(Like::getPostId)
                .distinct()
                .flatMap(noticeRepository::findById)   // postId → Notice
                .cast(Object.class);

        //커뮤니티 좋아요
        Mono<String> userIdMono = userRepository.findByUserEmail(email)
                .map(User::getUserId);

        Flux<Object> likedCommunities = userIdMono.flatMapMany(userId ->
                likeRepository.findAllByUserIdAndPostType(userId, COMMUNITY_TYPE)
                        .map(Like::getPostId)
                        .distinct()
                        .flatMap(communityPostRepository::findById)  // postId → CommunityPost
                        .cast(Object.class)
        );
        return Flux.merge(likedNotices, likedCommunities);
    }





    //내가 쓴 글 조회
    public Flux<CommunityResponseDto> getMyCommunityPosts(String userId) {
        return communityPostRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .flatMap(p ->
                        likeRepository.existsByUserIdAndPostIdAndPostType(userId, p.getId(), COMMUNITY_TYPE)
                                .zipWith(bookmarkRepository.existsByUserIdAndPostIdAndPostType(userId, p.getId(), COMMUNITY_TYPE))
                                .map(t -> toResponse(p, t.getT1(), t.getT2())) // ← viewer 상태 세팅
                );
    }




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
}
