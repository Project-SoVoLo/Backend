package soboro.soboro_web.webclient;

import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
// gemini ai를 사용한 챗봇 상담 내용 요약
public class GeminiApiClient {
    private final WebClient webClient;

    public GeminiApiClient(WebClient.Builder clientBuilder) {
        this.webClient = clientBuilder
                .baseUrl("http://localhost:5001/") // Gemini Flask 서버 주소
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // Gemini 요약 요청 메서드
    public Mono<Map<String, String>> summarizeChatLog(String chatLog){
        return webClient.post()
                .uri("/summarize")
                .bodyValue(Map.of("chatLog", chatLog))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .doOnError(e -> log.error("Gemini 요약 요청 실패", e));
    }
}
