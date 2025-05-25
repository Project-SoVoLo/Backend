package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.ChatSummary;
import soboro.soboro_web.dto.ChatSummaryRequest;
import soboro.soboro_web.service.ChatSummaryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final ChatSummaryService chatSummaryService;

    // 챗봇 상담 내역 요약 저장
    @PostMapping("/chat-summaries")
    public Mono<ResponseEntity<Void>> saveChatSummary(@RequestBody ChatSummaryRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(userEmail ->
                        chatSummaryService.summarizeAndSave(
                                userEmail,
                                request.getChatLog(),
                                request.getEmotionType() // 감정 상태 전달
                        ).thenReturn(ResponseEntity.ok().build())
                );
    }

    // 챗봇 상담 내역 요약 조회
    @GetMapping("/chat-summaries")
    public Flux<ChatSummary> getChatSummaries() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName()) // 인증된 사용자의 userId(Email)를 기준으로 반환
                .flatMapMany(chatSummaryService::getSummaries);
    }
}
