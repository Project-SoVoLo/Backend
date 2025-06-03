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

import static java.util.Locale.filter;

@Service
@RequiredArgsConstructor
public class ChatSummaryService {
    private final ChatSummaryRepository chatSummaryRepository;
    private final EmotionScoreRecordRepository emotionScoreRecordRepository;
    private final GeminiApiClient apiClient;

    // 상담 요약 및 저장 -> 저장되는 테이블을 emotion 으로 통합, 클래스는 gemini로 판단 x
    public Mono<Void> summarizeAndSave(String userEmail, String chatLog) {
        // 디버깅용 로그
        System.out.println("▶️ [서비스] 요약 시작 - userEmail: " + userEmail);
        System.out.println("📝 chatLog 전체 내용:\n" + chatLog);

        return apiClient.summarizeChatLog(chatLog)
                .flatMap(response -> {
                    String summaryText = response.get("summary");
                    System.out.println("📩 Gemini 응답: " + summaryText);

                    String[] parts = summaryText.split("\\|");
                    String summary = parts.length > 1 ? parts[1].trim() : "";
                    String feedback = parts.length > 2 ? parts[2].trim() : "";

                    System.out.println("📄 추출된 요약: " + summary);
                    System.out.println("💡 추출된 피드백: " + feedback);

                    // emotion 테이블에 저장되도록 수정
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));       // emotion에 저장되어있던 감정 점수 분석 결과를 조회함 -> 그 튜플에다가 요약본을 넣어야 하니까
                    System.out.println("📆 조회 기준 날짜 (today): " + today);

                    return emotionScoreRecordRepository.findAllByUserEmail(userEmail)
                            // 디버깅용으로 레코드 있는거 다 출력해보기 (날짜)
                            .doOnNext(r -> System.out.println("🔍 DB에 있는 날짜: " + r.getEmotionDate()))
                            .filter(record -> {
                                LocalDate date = record.getEmotionDate();
                                boolean match = date != null && date.isEqual(today);
                                if (!match) {
                                    System.out.println("!! 날짜 불일치: " + record.getEmotionDate());}
                                return match;
                            })
                            .next()
                            .flatMap(record -> {
                                System.out.println("✅ 날짜 매칭된 record 있음: " + record.getEmotionDate());
                                record.setSummary(summary);
                                record.setFeedback(feedback);
                                return emotionScoreRecordRepository.save(record)
                                        .doOnSuccess(saved -> System.out.println("💾 저장 완료: " + saved.getEmotionId()));
                            })
                            .switchIfEmpty(Mono.fromRunnable(() -> {
                                System.out.println("❌ 오늘 날짜에 해당하는 EmotionScoreRecord 없음 (filter 기준)");
                                System.out.println("⚠️ userEmail: " + userEmail);
                                System.out.println("------------------------------");
                            }));
                })
                .doOnError(e -> System.out.println("🚨 요약 저장 중 오류 발생: " + e.getMessage()))
                .then();

                    /*
                    return emotionScoreRecordRepository.findByUserEmailAndEmotionDate(userEmail, today)
                            .flatMap(record -> {
                                System.out.println("📦 기존 점수 레코드 있음! 업데이트 중...");
                                record.setSummary(summary);     // gemini가 준 상담내용 요약본
                                record.setFeedback(feedback);   // gemini가 준 피드백 요약본
                                return emotionScoreRecordRepository.save(record)
                                        .doOnSuccess(saved -> System.out.println("💾 저장 완료: emotionId=" + saved.getEmotionId()));
                            })
                            .switchIfEmpty(Mono.fromRunnable(() -> {
                                System.out.println("❌ 오늘 날짜에 해당하는 EmotionScoreRecord 없음!");
                                System.out.println("⚠️ userEmail: " + userEmail);
                            }));

                })
                .doOnError(e -> System.out.println("🚨 요약 저장 중 오류 발생: " + e.getMessage()))
                .then();
                     */

    }

    // 상담 요약 내용 조회
    public Flux<EmotionScoreRecord> getEmotionRecords(String userEmail) {
        return emotionScoreRecordRepository.findByUserEmailOrderByEmotionDateDesc(userEmail);
    }

    public Mono<EmotionScoreRecord> getEmotionScore(String userEmail, LocalDate date) {
        return emotionScoreRecordRepository.findByUserEmailOrderByEmotionDateDesc(userEmail,date);
    }
}
