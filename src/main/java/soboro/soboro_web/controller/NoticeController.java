package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.NoticeRequestDto;
import soboro.soboro_web.dto.NoticeResponseDto;
import soboro.soboro_web.service.NoticeService;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    // 1. 공지사항 목록 조회
    @GetMapping
    public Flux<NoticeResponseDto> getNoticeList(Authentication authentication) {
        String userId = (authentication != null) ? authentication.getName() : null;
        return noticeService.getNoticeList(userId);
    }

    // 2. 공지사항 작성(관리자만)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<NoticeResponseDto>> createNotice(
            @RequestBody NoticeRequestDto requestDto,
            Authentication authentication
    ){
        String adminId = authentication.getName();
        return noticeService.createNotice(requestDto, adminId)
                .map(ResponseEntity::ok);
    }

    // 3. 공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public Mono<ResponseEntity<NoticeResponseDto>> getNotice(@PathVariable String noticeId,
                                                             Authentication authentication) {
        String userId = authentication.getName();
        return noticeService.getNotice(noticeId, userId)
                .map(ResponseEntity::ok);
    }

    // 4. 공지사항 수정(관리자만)
    @PatchMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<NoticeResponseDto>> updateNotice(
            @PathVariable String noticeId,
            @RequestBody NoticeRequestDto requestDto,
            Authentication authentication
    ) {
        String adminId = authentication.getName();
        return noticeService.updateNotice(noticeId, requestDto, adminId)
                .map(ResponseEntity::ok);
    }

    // 5. 공지사항 삭제(관리자만)
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteNotice(
            @PathVariable String noticeId,
            Authentication authentication
    ){
        String adminId = authentication.getName();
        return noticeService.deleteNotice(noticeId, adminId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    // 6. 공지사항 좋아요 토글
    @PostMapping("/{noticeId}/like")
    public Mono<ResponseEntity<NoticeResponseDto>> toggleLike(
            @PathVariable String noticeId,
            Authentication authentication // 토큰에서 아이디 추출
    ){
        String userId = authentication.getName(); // 로그인한 사용자 아이디
        return noticeService.toggleLike(noticeId, userId)
                .map(ResponseEntity::ok);
    }
}
