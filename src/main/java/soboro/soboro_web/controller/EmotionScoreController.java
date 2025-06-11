package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.EmotionScoreRecord;
import soboro.soboro_web.service.EmotionScoreService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotion")
public class EmotionScoreController {

    private final EmotionScoreService emotionScoreService;

    @GetMapping("/scores")
    public Flux<EmotionScoreRecord> getMyScores(Principal principal) {
        String userEmail = principal.getName();
        return emotionScoreService.getUserScores(userEmail);
    }
}
