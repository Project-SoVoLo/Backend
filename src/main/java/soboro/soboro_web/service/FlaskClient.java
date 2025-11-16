package soboro.soboro_web.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/***************************************
 * phq-9 모델 서버에 text를 보내서 점수 예측 요청
 * **************************************/
@Service
public class FlaskClient {

    private final WebClient webClient = WebClient.create("http://13.125.43.47:5050");

    // 입력받은 text 를 서버에 보내서 점수 예측 요청하기
    public Mono<Map<String, Object>> requestPhqPrediction(String text) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // 서버로부터 받은 5개 그룹에 대한 yes or no를 점수로 매핑하여 결과에 추가
    // 점수 매핑 방법 : group 1~4는 각 5점을 주고, 심각도가 높은 group 5 (자살)은 7점을 부여하여 총점은 0~27 사이 점수로 나오게 함
    public Map<String, Object> wrapWithScore(Map<String, Object> flaskResponse) {
        Map<String, Object> response = new HashMap<>();
        int totalScore = 0;

        Object resultObj = flaskResponse.get("result");

        if (!(resultObj instanceof Map)) {
            response.put("result", flaskResponse);
            response.put("phq9_total", 0);
            return response;
        }

        Map<String, Object> groupMap = (Map<String, Object>) resultObj;

        for (String group : groupMap.keySet()) {
            Object raw = groupMap.get(group);

            if (!(raw instanceof Map)) continue;

            Map<String, Object> rawMap = (Map<String, Object>) raw;

            Object predictedValue = rawMap.get("predicted");
            boolean predicted = predictedValue != null && predictedValue.toString().equals("1");

            int score = predicted
                    ? (group.equals("Group_5") ? 7 : 5)
                    : 0;

            totalScore += score;
        }

        response.put("result", flaskResponse);
        response.put("phq9_total", totalScore);
        return response;
    }
}
