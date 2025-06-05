package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.EmotionScoreRecord;

public interface EmotionScoreRepository extends ReactiveMongoRepository<EmotionScoreRecord, String> {
    //Flux<EmotionScoreRecord> findAllByUserIdOrderByEmotionDate(String userId);
}
