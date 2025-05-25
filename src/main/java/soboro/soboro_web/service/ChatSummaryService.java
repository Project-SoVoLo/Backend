package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.ChatSummary;
import soboro.soboro_web.domain.enums.EmotionTypes;
import soboro.soboro_web.repository.ChatSummaryRepository;
import soboro.soboro_web.webclient.GeminiApiClient;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ChatSummaryService {
    private final ChatSummaryRepository chatSummaryRepository;
    private final GeminiApiClient apiClient;

    // 상담 요약 및 저장
    public Mono<Void> summarizeAndSave(String userId, String chatLog, EmotionTypes emotionType) {
        return apiClient.summarizeChatLog(chatLog)
                .flatMap(response -> {
                    String summaryText = response.get("summary");

                    String[] parts = summaryText.split("\\|");
                    String summary = parts.length > 1 ? parts[1].trim() : "";
                    String feedback = parts.length > 2 ? parts[2].trim() : "";

                    ChatSummary chatSummary = new ChatSummary();
                    chatSummary.setUserId(userId);
                    chatSummary.setDate(LocalDate.now(ZoneId.of("Asia/Seoul")));
                    chatSummary.setSummary(summary);
                    chatSummary.setFeedback(feedback);
                    chatSummary.setEmotionType(emotionType); // 외부에서 받은 감정 상태 직접 설정

                    return chatSummaryRepository.save(chatSummary).then();
                });
    }

    // 상담 요약 내용 조회
    public Flux<ChatSummary> getSummaries(String userId){
        return chatSummaryRepository.findByUserIdOrderByDateDesc(userId);
    }

}
