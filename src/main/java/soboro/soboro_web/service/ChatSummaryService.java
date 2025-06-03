package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.ChatSummary;
import soboro.soboro_web.domain.EmotionScoreRecord;
import soboro.soboro_web.domain.enums.EmotionTypes;
import soboro.soboro_web.repository.ChatSummaryRepository;
import soboro.soboro_web.repository.EmotionScoreRecordRepository;
import soboro.soboro_web.webclient.GeminiApiClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSummaryService {
    private final ChatSummaryRepository chatSummaryRepository;
    private final EmotionScoreRecordRepository emotionScoreRecordRepository;
    private final GeminiApiClient apiClient;

    // 상담 요약 및 저장 -> 저장되는 테이블을 emotion 으로 통합, 클래스는 gemini로 판단 x
    public Mono<Void> summarizeAndSave(String userEmail, String chatLog) {
        return apiClient.summarizeChatLog(chatLog)
                .flatMap(response -> {
                    String summaryText = response.get("summary");

                    String[] parts = summaryText.split("\\|");
                    String summary = parts.length > 1 ? parts[1].trim() : "";
                    String feedback = parts.length > 2 ? parts[2].trim() : "";

                    // emotion 테이블에 저장되도록 수정
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));       // emotion에 저장되어있던 감정 점수 분석 결과를 조회함 -> 그 튜플에다가 요약본을 넣어야 하니까
                    return emotionScoreRecordRepository.findByUserEmailOrderByEmotionDateDesc(userEmail, today)
                            .flatMap(record -> {
                                record.setSummary(summary);     // gemini가 준 상담내용 요약본
                                record.setFeedback(feedback);   // gemini가 준 피드백 요약본
                                return emotionScoreRecordRepository.save(record);
                            });

                }).then();
    }

    // 상담 요약 내용 조회
    public Flux<EmotionScoreRecord> getEmotionRecords(String userEmail) {
        return emotionScoreRecordRepository.findByUserEmailOrderByEmotionDateDesc(userEmail);
    }

    public Mono<EmotionScoreRecord> getEmotionScore(String userEmail, LocalDate date) {
        return emotionScoreRecordRepository.findByUserEmailOrderByEmotionDateDesc(userEmail,date);
    }
}
