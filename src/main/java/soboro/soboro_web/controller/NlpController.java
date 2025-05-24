package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import soboro.soboro_web.service.GoogleNlpService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nlp")
public class NlpController {
    private final GoogleNlpService googleNlpService;

    @PostMapping("/emotion_class")
    public ResponseEntity<Map<String, Object>> analyzeEmotionClass(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        Map<String, Object> result = googleNlpService.analyzeSentiment(text);
        return ResponseEntity.ok(result);
    }

}
