package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.DiagnosisRecord;

public interface DiagnosisRecordRepository extends ReactiveMongoRepository<DiagnosisRecord, String> {
    // 특정 사용자(userId)의 자가진단 결과 전체 조회
    Flux<DiagnosisRecord> findAllByUserId(String userId);
}
