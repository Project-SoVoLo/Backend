package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.DiagnosisQuestion;
import soboro.soboro_web.domain.enums.DiagnosisType;

public interface DiagnosisQuestionRepository extends ReactiveMongoRepository<DiagnosisQuestion, String> {
    Flux<DiagnosisQuestion> findAllByTypeOrderByNumber(DiagnosisType type);
}
