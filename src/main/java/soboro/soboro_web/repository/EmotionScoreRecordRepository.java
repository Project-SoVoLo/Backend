package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.EmotionScoreRecord;

import java.time.LocalDate;
import java.util.List;

public interface EmotionScoreRecordRepository extends ReactiveMongoRepository<EmotionScoreRecord, String> {
    // 저장할 때 사용
    Mono<EmotionScoreRecord> findByUserEmailAndEmotionDate(String userEmail, LocalDate emotionDate);
    Flux<EmotionScoreRecord> findByUserEmail(String userEmail);
    Flux<EmotionScoreRecord> findAllByUserEmail(String userEmail);


    // 저장된 점수 조회 - 사용자 이메일로 조회
    Mono<EmotionScoreRecord> findByUserEmailOrderByEmotionDateDesc(String userEmail, LocalDate emotionDate);
    Flux<EmotionScoreRecord> findByUserEmailOrderByEmotionDateDesc(String userEmail);

}