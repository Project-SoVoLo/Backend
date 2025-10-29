package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;
import soboro.soboro_web.domain.Card;
import soboro.soboro_web.dto.CardResponseDto;
import soboro.soboro_web.repository.BookmarkRepository;
import soboro.soboro_web.repository.CardRepository;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final BookmarkRepository bookmarkRepository;

    // 1. 전체 카드뉴스 목록 (로그인 사용자 기준 북마크 여부 포함)
    public Flux<CardResponseDto> getAllCards(String userId) {
        return cardRepository.findAll()
                .flatMap(card ->
                        bookmarkRepository.findByUserIdAndPostId(userId, card.getPostId())
                                .map(b -> true)
                                .defaultIfEmpty(false)
                                .map(bookmarked -> toDto(card, bookmarked))
                );
    }

    // 2. 카드뉴스 상세 조회 (로그인 사용자 기준 북마크 여부 포함)
    public Mono<CardResponseDto> getCardById(String userId, String cardId) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new RuntimeException("카드를 찾을 수 없습니다.")))
                .flatMap(card ->
                        bookmarkRepository.findByUserIdAndPostId(userId, cardId)
                                .map(b -> true)
                                .defaultIfEmpty(false)
                                .map(bookmarked -> toDto(card, bookmarked))
                );
    }

    // 3. 카드뉴스 작성 (관리자만)
    public Mono<Card> createCard(Card card) {
        return cardRepository.save(card);
    }

    // 4. 카드뉴스 삭제 (관리자만)
    public Mono<Void> deleteCard(String id) {
        return cardRepository.deleteById(id);
    }

    // 5. 북마크 토글
    public Mono<CardResponseDto> toggleBookmark(String userId, String postId) {
        return cardRepository.findById(postId)
                .switchIfEmpty(Mono.error(new RuntimeException("카드를 찾을 수 없습니다.")))
                .flatMap(card ->
                        bookmarkRepository.findByUserIdAndPostId(userId, postId)
                                // 이미 북마크 되어 있으면 삭제
                                .flatMap(existing ->
                                        bookmarkRepository.delete(existing)
                                                .thenReturn(false) // bookmarked = false
                                )
                                // 북마크 안 되어 있으면 추가
                                .switchIfEmpty(
                                        bookmarkRepository.save(new Bookmark(userId, postId))
                                                .thenReturn(true) // bookmarked = true
                                )
                                // 최종 DTO 구성
                                .map(bookmarked -> {
                                    CardResponseDto dto = new CardResponseDto();
                                    dto.setPostId(card.getPostId());
                                    dto.setTitle(card.getTitle());
                                    dto.setContent(card.getContent());
                                    dto.setDate(card.getDate());
                                    dto.setAdminId(card.getAdminId());
                                    dto.setImageUrls(card.getImageUrls());
                                    dto.setThumbnailUrl(card.getThumbnailUrl());
                                    dto.setBookmarked(bookmarked);
                                    return dto;
                                })
                );
    }
    // Entity → DTO 변환 헬퍼
    private CardResponseDto toDto(Card card, boolean bookmarked) {
        CardResponseDto dto = new CardResponseDto();
        dto.setPostId(card.getPostId());
        dto.setTitle(card.getTitle());
        dto.setContent(card.getContent());
        dto.setDate(card.getDate());
        dto.setAdminId(card.getAdminId());
        dto.setImageUrls(card.getImageUrls());
        dto.setThumbnailUrl(card.getThumbnailUrl());
        dto.setBookmarked(bookmarked);
        return dto;
    }
}
