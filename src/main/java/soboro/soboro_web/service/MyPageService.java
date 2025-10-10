package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.*;
import soboro.soboro_web.repository.BookmarkRepository;
import soboro.soboro_web.repository.CardRepository;
import soboro.soboro_web.repository.LikeRepository;
import soboro.soboro_web.repository.NoticeRepository;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final BookmarkRepository bookmarkRepository;
    private final CardRepository cardRepository;
    private final LikeRepository likeRepository;
    private final NoticeRepository noticeRepository;

    // 내가 북마크한 게시글 전체 조회 (카드뉴스 + 커뮤니티)
    public Flux<Object> getAllBookmarks(String userId) {
        // 1. 내가 북마크한 postId 목록 가져오기
        Flux<String> bookmarkedPostIds = bookmarkRepository.findAllByUserId(userId)
                .map(Bookmark::getPostId)
                .distinct();

        // 2. 카드뉴스 + 커뮤니티 둘 다 postId로 조회
        Flux<Card> bookmarkedCards = bookmarkedPostIds.flatMap(cardRepository::findById);

            /*
                커뮤니티 북마크 게시글 조회
                Flux<Community> bookmarkedCommunities = ;
             */

        // 3. 합쳐서 리턴
        return Flux.merge(bookmarkedCards, bookmarkedCommunities)
                .cast(Object.class);
    }

    // 내가 좋아요한 게시글 전체 조회 (공지사항 + 커뮤니티)
    public Flux<Object> getAllLikes(String userId) {
        // 1. 내가 좋아요한 모든 postId 목록 가져오기
        Flux<String> likedPostIds = likeRepository.findAllByUserId(userId)
                .map(Like::getPostId)
                .distinct();

        // 2. 공지 + 커뮤니티 좋아요 게시글 병렬 조회
        Flux<Notice> likedNotices = likedPostIds
                .flatMap(noticeRepository::findById);

            /*
                커뮤니티 좋아요 게시글 조회
                 Flux<Community> likedCommunities = ;
             */


        // 3.합쳐서 리턴
        return Flux.merge(likedNotices, likedCommunities)
                .cast(Object.class);
    }

}
