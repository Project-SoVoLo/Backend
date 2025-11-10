package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;
import soboro.soboro_web.domain.Card;
import soboro.soboro_web.dto.CardResponseDto;
import soboro.soboro_web.service.CardService;
import soboro.soboro_web.service.S3Service;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;
    private final S3Service s3Service;

    // 1. 전체 카드뉴스 조회 (북마크 여부 포함)
    @GetMapping
    public Flux<CardResponseDto> getAllCards(Authentication authentication) {
        String userId = (authentication != null) ? authentication.getName() : null;
        return cardService.getAllCards(userId);
    }

    // 2. 카드뉴스 상세 조회 (북마크 여부 포함)
    @GetMapping("/{cardId}")
    public Mono<ResponseEntity<CardResponseDto>> getCard(
            @PathVariable String cardId,
            Authentication authentication
    ) {
        String userId = (authentication != null) ? authentication.getName() : null;
        return cardService.getCardById(userId, cardId)
                .map(ResponseEntity::ok);
    }

    // 3. 카드뉴스 작성 (관리자만)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<CardResponseDto>> createCard(
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart("thumbnail") FilePart thumbnailFile,
            @RequestPart(value = "images", required = false) Flux<FilePart> imageFiles,
            Authentication authentication
    ) {
        String adminId = authentication.getName();

        Mono<String> thumbnailUrlMono = s3Service.uploadFile(thumbnailFile, "cards/thumbnails");

        Mono<List<String>> imageUrlsMono = (imageFiles == null ? Flux.<FilePart>empty() : imageFiles)
                .flatMap(file -> s3Service.uploadFile(file, "cards/images"))
                .collectList();

        return Mono.zip(thumbnailUrlMono, imageUrlsMono)
                .flatMap(tuple -> {
                    String thumbnailUrl = tuple.getT1();
                    List<String> imageUrls = tuple.getT2();

                    return cardService.createCardWithImage(
                            title,
                            content,
                            adminId,
                            thumbnailUrl,
                            imageUrls
                    );
                })
                .map(card -> {
                    CardResponseDto dto = new CardResponseDto();
                    dto.setPostId(card.getPostId());
                    dto.setTitle(card.getTitle());
                    dto.setContent(card.getContent());
                    dto.setDate(card.getDate());
                    dto.setAdminId(card.getAdminId());
                    dto.setThumbnailUrl(card.getThumbnailUrl());
                    dto.setImageUrls(card.getImageUrls());
                    dto.setBookmarked(false);
                    return ResponseEntity.ok(dto);
                });
    }

    // 4. 카드뉴스 삭제 (관리자만)
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteCard(@PathVariable String cardId) {
        return cardService.deleteCard(cardId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    // 5. 북마크 토글 (로그인 사용자만)
    @PostMapping("/{cardId}/bookmark")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<CardResponseDto>> toggleBookmark(
            @PathVariable String cardId,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        return cardService.toggleBookmark(userId, cardId)
                .map(ResponseEntity::ok);
    }
}