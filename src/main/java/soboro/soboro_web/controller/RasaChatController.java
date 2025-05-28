package soboro.soboro_web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import soboro.soboro_web.service.RasaChatService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// 기존 로직을 service로 옮김 (통합 컨트롤러를 위해서)
@RestController
@RequestMapping("/api/rasa")
@RequiredArgsConstructor
public class RasaChatController {
    private final RasaChatService rasaChatService;

    @PostMapping("/classification")
    public ResponseEntity<Map<String, String>> classifyAndSend(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        String sender = (String) body.get("sender");
        Integer phqScore = body.get("phq_score") != null ? Integer.parseInt(body.get("phq_score").toString()) : null;
        String emotion = (String) body.get("google_emotion");

        String reply = rasaChatService.classifyAndSendToRasa(message, sender, phqScore, emotion);
        return ResponseEntity.ok(Map.of("response", reply));
    }
}

