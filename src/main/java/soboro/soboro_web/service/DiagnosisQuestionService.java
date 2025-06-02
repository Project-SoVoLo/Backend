package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.DiagnosisQuestion;
import soboro.soboro_web.domain.enums.DiagnosisType;
import soboro.soboro_web.repository.DiagnosisQuestionRepository;

@Service
@RequiredArgsConstructor
public class DiagnosisQuestionService {
    private final DiagnosisQuestionRepository repository;

    public Flux<DiagnosisQuestion> getQuestionsByType(DiagnosisType type) {
        return repository.findAllByTypeOrderByNumber(type);
    }
}

