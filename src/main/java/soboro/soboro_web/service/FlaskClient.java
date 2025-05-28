package soboro.soboro_web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/***************************************
 * phq-9 모델 서버에 text를 보내서 점수 예측 요청
 * **************************************/
@Service
public class FlaskClient {

    private final WebClient webClient = WebClient.create("http://localhost:5050");

    public Mono<Map> requestPhqPrediction(String text) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(Map.class);
    }
}
