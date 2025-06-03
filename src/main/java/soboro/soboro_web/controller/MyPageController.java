package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.ChatSummary;
import soboro.soboro_web.domain.EmotionScoreRecord;
import soboro.soboro_web.domain.enums.EmotionTypes;
import soboro.soboro_web.dto.ChatSummaryRequest;
import soboro.soboro_web.dto.ChatSummaryResponse;
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
                                request.getChatLog()
                        ).thenReturn(ResponseEntity.ok().build())
                );
    }

    // 챗봇 상담 내역 요약 조회 + 점수까지 같이 조회
    @GetMapping("/chat-summaries")
    public Flux<ChatSummaryResponse> getChatSummaries() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    String email = ctx.getAuthentication().getName();   // 디버깅용 - 현재 로그인된 사용자 조회 (이메일로 찍히는지)
                    System.out.println("현재 사용자 이메일: "+ email);
                    return email;
                })
                .flatMapMany(userEmail ->
                        chatSummaryService.getEmotionRecords(userEmail)
                                .flatMap(chatSummary ->
                                        chatSummaryService.getEmotionRecords(userEmail)
                                                .map(record -> new ChatSummaryResponse(
                                                        record.getEmotionDate(),
                                                        record.getSummary(),
                                                        record.getFeedback(),
                                                        record.getEmotionType().getKorean(),
                                                        record.getEmotionType().getColorCode(),
                                                        record.getPhqScore(),
                                                        record.getGoogleEmotion()
                                                ))));
    }

}

