package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.EmotionScoreRecord;
import soboro.soboro_web.domain.enums.EmotionTypes;
import soboro.soboro_web.repository.EmotionScoreRecordRepository;
import soboro.soboro_web.service.FlaskClient;
import soboro.soboro_web.service.GoogleNlpService;
import soboro.soboro_web.service.RasaChatService;

import java.time.LocalDate;
import java.util.List;
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
    private final RasaChatService rasaChatService;
    private final EmotionScoreRecordRepository emotionScoreRecordRepository;

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);


    // 대화 시작용 엔드포인트 메소드
    @PostMapping("/start-chat")
    public ResponseEntity<Map<String, Object>> startChat(@RequestBody Map<String, String> request) {
        String sender = request.get("sender");
        String message = request.getOrDefault("message", "안녕"); // 기본 메시지 설정

        Map<String, Object> rasaRequest = Map.of(
                "sender", sender,
                "message", message
        );

        RestTemplate restTemplate = new RestTemplate();
        String rasaUrl = "http://localhost:5005/webhooks/rest/webhook"; // Rasa 기본 REST endpoint

        ResponseEntity<List> response = restTemplate.postForEntity(rasaUrl, rasaRequest, List.class);
        Map<String, Object> result = Map.of("response", response.getBody());
        return ResponseEntity.ok(result);
    }


    // 사용자가 오늘 하루 처음 말한 감정 얘기는 감정 분석하여 저장
    @PostMapping("/full")
    public Mono<ResponseEntity<Map<String, Object>>> analyzeAndSendToRasa(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sender = request.get("sender"); // 사용자 ID 필요

        // 1. PHQ9 예측 -> output : 원본텍스트 + 각 문항에 대한 yes(1) or no(0) + yes or no를 점수로 매핑한 total score
        return flaskClient.requestPhqPrediction(text)
                .map(flaskClient::wrapWithScore)
                .flatMap(phqResult -> {
                    int phqScore = (int) phqResult.get("phq9_total");
                    // 확인용 phq 결과
                    for (Map.Entry<String, Object> entry : phqResult.entrySet()) {
                        log.info("PHQ 그룹: {} → 예측값: {}", entry.getKey(), entry.getValue());
                    }

                    // 2. 사용자 입력 텍스트 -> Google NLP 감정 분석 -> output : 구글 점수로부터 분기한 positive/neutral/negative
                    Map<String, Object> sentimentResult = googleNlpService.analyzeSentiment(text);
                    String googleEmotion = (String) sentimentResult.get("sentiment");
                    log.info("📌 감정 분석 결과 (Google NLP): {}", googleEmotion);     // 확인용 구글 결과

                    // 3. 원본 텍스트 + phq9 점수 + 구글 클래스 -> Rasa로 전송 -> 케이스에 따라서 응답 받기
                    Map<String, Object> combinedData = Map.of(
                            "message", text,
                            "sender", sender,
                            "phq_score", phqScore,
                            "google_emotion", googleEmotion
                    );

//                    // 디버깅용 코드 - rasa 전달 x
//                    return Mono.fromCallable(() ->
//                            ResponseEntity.ok(combinedData)
//                    );

                    // 4. Rasa 서버에 전달하기
                    return Mono.fromCallable(() -> {
                        String rasaUrl = "http://localhost:8080/api/rasa/classification";
                        RestTemplate restTemplate = new RestTemplate();
                        ResponseEntity<Map> response = restTemplate.postForEntity(rasaUrl, combinedData, Map.class);

                        // 타입캐스팅
                        Object raw = response.getBody().get("response");

                        List<Map<String, Object>> rasaResponses;

                        if (raw instanceof List) {
                            rasaResponses = (List<Map<String, Object>>) raw;
                        } else if (raw instanceof String) {
                            rasaResponses = List.of(Map.of("text", raw));
                        } else {
                            throw new RuntimeException("예상치 못한 Rasa 응답 형식");
                        }

                        System.out.println("✅ Rasa 응답 확인: " + response.getBody());


                        // DB에 phq_score, google_emotion 저장하기 (EmotionScoreRecord)
                        EmotionScoreRecord  record = new EmotionScoreRecord();
                        record.setUserEmail(sender);
                        record.setEmotionDate(LocalDate.now());
                        record.setPhqScore(phqScore);
                        record.setGoogleEmotion(googleEmotion);
                        try {
                            EmotionTypes emotionTypes = EmotionTypes.valueOf(googleEmotion.toUpperCase());
                            record.setEmotionType(emotionTypes);
                        } catch (IllegalArgumentException e) {
                            log.warn("Emotion Type 매핑 실패: {}", googleEmotion);
                        }
                        emotionScoreRecordRepository.save(record).subscribe();

                      return ResponseEntity.ok(Map.of("response", rasaResponses));
                    });

                });
    }


    // 대화 연장 이후 나누는 얘기들은 이전에 기록된 감정 클래스를 참고하여
    // rasa에서 intent 별로 응답만 받도록 함, 별도 감정 분석 x
    @PostMapping("/continue")
    public Mono<ResponseEntity<Map<String, Object>>> continueChat(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String sender = request.get("sender");

        return emotionScoreRecordRepository
                .findByUserEmailOrderByEmotionDateDesc(sender)
                .next() // Flux → Mono로 바꾸고 최신 기록 1개 가져옴
                .defaultIfEmpty(new EmotionScoreRecord()) // 없을 경우 기본값
                .map(record -> {
                    String emotionClass = record.getEmotionType() != null
                            ? record.getEmotionType().name().toLowerCase()
                            : "neutral";

                    Map<String, Object> rasaRequest = Map.of(
                            "message", text,
                            "sender", sender,
                            "class", emotionClass
                    );

                    RestTemplate restTemplate = new RestTemplate();
                    String rasaUrl = "http://localhost:5005/webhooks/rest/webhook";
                    ResponseEntity<List> response = restTemplate.postForEntity(rasaUrl, rasaRequest, List.class);
                    return ResponseEntity.ok(Map.of("response", response.getBody()));
                });
    }



}
