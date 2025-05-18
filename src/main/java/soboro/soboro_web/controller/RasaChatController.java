package soboro.soboro_web.controller;

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

import java.util.HashMap;
import java.util.Map;

/*
* 사용자의 채팅 입력을 Rasa로 보내는 API 컨트롤러
* */

@RestController
@RequestMapping("/api/rasa")
public class RasaChatController {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String rasaUrl = "http://localhost:5005/webhooks/rest/webhook";

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");

        Map<String, String> payload = new HashMap<>();
        payload.put("sender", "user1");  // 유저 ID (고정 또는 세션에 따라 달리할 수 있음)
        payload.put("message", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(rasaUrl, request, String.class);

        return ResponseEntity.ok(response.getBody());
    }
}
