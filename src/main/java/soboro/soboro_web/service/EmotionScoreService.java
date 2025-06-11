package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.EmotionScoreRecord;
import soboro.soboro_web.repository.EmotionScoreRecordRepository;
import soboro.soboro_web.repository.EmotionScoreRepository;

@Service
@RequiredArgsConstructor
public class EmotionScoreService {
    private final EmotionScoreRecordRepository repository;

    public Flux<EmotionScoreRecord> getUserScores(String userEmail) {
        return repository.findByUserEmailOrderByEmotionDateDesc(userEmail);
    }
}