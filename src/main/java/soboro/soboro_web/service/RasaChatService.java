package soboro.soboro_web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*****************************************************************************
* input : userId + text + phq9_score + google_emotion
* ⬇
* phq9_score 또는 google emotion에 따라서 -> positive / neutral / negative 로 분기
* ⬇
* Rasa 서버로 전달하여 피드백 받기
* ****************************************************************************/

@Service
public class RasaChatService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String rasaUrl = "http://localhost:5005/webhooks/rest/webhook";

    public String classifyAndSendToRasa(String message, String sender, Integer phqScore, String emotion) {
        // 클래스 분류
        String userClass = "unknown";
        if (phqScore != null) {
            if (phqScore <= 4) userClass = "positive";
            else if (phqScore <= 14) userClass = "neutral";
            else userClass = "negative";
        } else if (emotion != null) {
            userClass = switch (emotion.toLowerCase()) {
                case "positive", "neutral", "negative" -> emotion.toLowerCase();
                default -> "unknown";
            };
        }

        System.out.println("📌 분류 class: " + userClass);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        // 슬롯 리셋
        String slotSetUrl = "http://localhost:5005/conversations/" + sender + "/tracker/events";
        Map<String, Object> resetPayload = Map.of("event", "reset_slots");
        restTemplate.postForEntity(slotSetUrl, new HttpEntity<>(resetPayload, headers), String.class);

        // 슬롯 설정
        Map<String, Object> slotPayload = new HashMap<>();
        slotPayload.put("event", "slot");
        slotPayload.put("name", "class");
        slotPayload.put("value", userClass);
        restTemplate.postForEntity(slotSetUrl, new HttpEntity<>(slotPayload, headers), String.class);

        // 잠깐 대기
        try { Thread.sleep(300); } catch (InterruptedException e) { e.printStackTrace(); }

        // 메세지 전달
        Map<String, Object> msgPayload = Map.of(
                "sender", sender,
                "message", message
        );

        ResponseEntity<String> rasaResponse = restTemplate.postForEntity(
                rasaUrl, new HttpEntity<>(msgPayload, headers), String.class
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rasaResponse.getBody());

            if (root.isArray() && root.size() > 0) {
                return root.get(0).get("text").asText();
            } else {
                return "챗봇 응답이 없습니다.";
            }
        } catch (Exception e) {
            return "❌ 응답 파싱 오류: " + e.getMessage();
        }
    }
}
