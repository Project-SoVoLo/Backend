package soboro.soboro_web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*
* 모델 결과+채팅내용을  Rasa로 보내는 API 컨트롤러
* - 입력 : 모델이 분류한 점수+텍스트
* - 출력 : 클래스 분류 + 텍스트 원본 -> Rasa로 이 두 개의 값을 같이 보냄
* */

@RestController
@RequestMapping("/api/rasa")
public class RasaChatController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String rasaUrl = "http://localhost:5005/webhooks/rest/webhook";

    // 사용자의 message, phq_score, google_emotion을 받아서
    // 긍정/중립/부정 분기 후에 텍스트와 같이 rasa 서버로 전달
    @PostMapping("/classification")
    public ResponseEntity<Map<String, String>> classifyAndSentToRasa(@RequestBody Map<String, Object> body) {

        // 사용자로부터 입력받은 phq 점수
        Integer phqScore = null;
        Object rawScore = body.get("phq_score");
        if (rawScore != null) {
            try {
                phqScore = Integer.parseInt(rawScore.toString());
            } catch (NumberFormatException e) {
                System.out.println("phq_score 값이 숫자가 아닙니다: " + rawScore);
            }
        }
        // 사용자로부터 입력받은 google 분류
        String message = (String) body.get("message");
        String emotion = (String) body.get("google_emotion");
        // 같이 전달받은 현재 사용자 아이디
        String userId = (String) body.get("sender");

        // 감정 분기 클래스 판단
        String userClass = "unknown";
        if (phqScore != null) { // phq 점수가 있을땐 -> 점수 범위에 따라서 판단
            if (phqScore <= 4) userClass = "positive";
            else if (phqScore <= 14) userClass = "neutral";
            else if (phqScore <= 27) userClass = "negative";
        } else if (emotion != null) {   // phq 점수 없고, google 있을땐 -> 해당 감정 클래스 그대로 사용
            userClass = switch(emotion.toLowerCase()) {
                case "positive", "neutral", "negative" -> emotion.toLowerCase();
                default -> "unknown";
            };
        }

        // 디버깅용 로그 출력
        System.out.println("📌 사용자 입력 로그");
        System.out.println("💬 message: " + message);
        System.out.println("📊 phq_score: " + phqScore);
        System.out.println("🧠 google_emotion: " + emotion);
        System.out.println("📦 분류된 class = " + userClass + " (타입: " + userClass.getClass().getSimpleName() + ")");

        // rest api 전달을 위한 형태
        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));


        // slot 설정
        String slotSetUrl = "http://localhost:5005/conversations/" + userId + "/tracker/events";

        // 이전 세션의 slot이 남아있는 경우를 대비하여 초기화
        Map<String, Object> resetPayload = Map.of(
                "event", "reset_slots"
        );
        restTemplate.postForEntity(slotSetUrl, new HttpEntity<>(resetPayload, headers), String.class);

        // slot과 함께 rasa에 전달
        Map<String, Object> slotPayload = new HashMap<>();
        slotPayload.put("event", "slot");
        slotPayload.put("name", "class");
        slotPayload.put("value", String.valueOf(userClass));
        restTemplate.postForEntity(slotSetUrl, new HttpEntity<>(slotPayload, headers), String.class);
        // 반영 대기
        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }

        // 메세지 전송
        Map<String, Object> msgPayload = Map.of(
                "sender", userId,
                "message", message
        );

        ResponseEntity<String> rasaResponse = restTemplate.postForEntity(
                rasaUrl, new HttpEntity<>(msgPayload, headers), String.class
        );

        // 3. text 필드만 파싱해서 꺼내기
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rasaResponse.getBody());

            if (root.isArray() && root.size() > 0) {
                String reply = root.get(0).get("text").asText();
                return ResponseEntity.ok(Map.of("response", reply));
            } else {
                return ResponseEntity.ok(Map.of("response", "챗봇 응답이 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "응답 파싱 중 오류 발생", "detail", e.getMessage()));
        }
    }
}
