package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;
import soboro.soboro_web.domain.Card;
import soboro.soboro_web.repository.BookmarkRepository;
import soboro.soboro_web.repository.CardRepository;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final BookmarkRepository bookmarkRepository;

    // 1. 전체 카드뉴스 목록
    public Flux<Card> getAllCards(){
        return cardRepository.findAll();
    }

    // 2. 카드뉴스 상세 조회
    public Mono<Card> getCardById(String id){
        return cardRepository.findById(id);
    }

    // 3. 카드뉴스 작성
    public Mono<Card> createCard(Card card){
        return cardRepository.save(card);
    }

    // 4. 카드뉴스 삭제
    public Mono<Void> deleteCard(String id){
        return cardRepository.deleteById(id);
    }

    // 5. 북마크 토글
    public Mono<Bookmark> toggleBookmark(String userId, String postId) {
        return bookmarkRepository.findByUserIdAndPostId(userId, postId)
                .flatMap(existing ->  // 이미 북마크한 경우 → 삭제
                        bookmarkRepository.delete(existing)
                                .then(Mono.just(existing))
                )
                .switchIfEmpty(       // 북마크 안 되어있으면 → 추가
                        bookmarkRepository.save(createBookmark(userId, postId))
                );
    }

    private Bookmark createBookmark(String userId, String postId) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(userId);
        bookmark.setPostId(postId);
        return bookmark;
    }
}
