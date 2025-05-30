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
                                request.getChatLog(),
                                request.getEmotionType() // 감정 상태 전달
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
//                .map(ctx -> ctx.getAuthentication().getName()) // 인증된 사용자의 userId(Email)를 기준으로 반환
                .flatMapMany(userEmail ->
                        chatSummaryService.getSummaries(userEmail)
                                .flatMap(chatSummary ->
                                        chatSummaryService.getEmotionScore(userEmail, chatSummary.getDate())
                                                .defaultIfEmpty(new EmotionScoreRecord())   // phq랑 google 이 다른 테이블에 있으니까, 일단 이게 없어도 출력하도록 함
                                                .map(emotionScore -> new ChatSummaryResponse(
                                                        chatSummary.getDate(),
                                                        chatSummary.getSummary(),
                                                        chatSummary.getFeedback(),
                                                        chatSummary.getEmotionType().getKorean(),
                                                        chatSummary.getEmotionType().getColorCode(),
                                                        emotionScore.getPhqScore(),
                                                        emotionScore.getGoogleEmotion()
                                                ))));
    }

}

