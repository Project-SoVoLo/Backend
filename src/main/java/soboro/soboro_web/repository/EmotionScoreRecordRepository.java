package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import soboro.soboro_web.domain.EmotionScoreRecord;

public interface EmotionScoreRecordRepository extends ReactiveMongoRepository<EmotionScoreRecord, String> {
}