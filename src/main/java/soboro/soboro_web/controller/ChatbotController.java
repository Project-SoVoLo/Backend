package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import soboro.soboro_web.service.FlaskClient;
import soboro.soboro_web.service.GoogleNlpService;

import java.util.Map;

/**********************************************************************************
* 사용자 입력 텍스트를 받아서 -> phq 점수 측정 & 구글 점수 측정 -> rasa 서버 전달
* 를 실행하는 챗봇 전체 컨트롤러
* *********************************************************************************/

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatbotController {
    private final FlaskClient flaskClient;
    private final GoogleNlpService googleNlpService;

    @PostMapping("/full")
    public Mono<ResponseEntity<Map<String, Object>>> analyzeAndSendToRasa(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sender = request.get("sender"); // 사용자 ID 필요

        // 1. PHQ9 예측 -> output : 원본텍스트 + 각 문항에 대한 yes(1) or no(0) + yes or no를 점수로 매핑한 total score
        return flaskClient.requestPhqPrediction(text)
                .map(flaskClient::wrapWithScore)
                .flatMap(phqResult -> {
                    int phqScore = (int) phqResult.get("phq9_total");

                    // 2. 사용자 입력 텍스트 -> Google NLP 감정 분석 -> output : 구글 점수로부터 분기한 positive/neutral/negative
                    Map<String, Object> sentimentResult = googleNlpService.analyzeSentiment(text);
                    String googleEmotion = (String) sentimentResult.get("sentiment");

                    // 3. 원본 텍스트 + phq9 점수 + 구글 클래스 -> Rasa로 전송 -> 케이스에 따라서 응답 받기
                    Map<String, Object> combinedData = Map.of(
                            "message", text,
                            "sender", sender,
                            "phq_score", phqScore,
                            "google_emotion", googleEmotion
                    );

                    // Spring에서 내부 호출처럼 RestTemplate을 직접 쓰지 않고 외부 전달할 경우엔 RasaChatController를 Service로 분리해도 좋음!
                    return Mono.fromCallable(() ->
                            ResponseEntity.ok(combinedData)  // 💡 여기서 바로 combinedData 반환 (디버깅 확인용)
                    );
                });
    }
}
