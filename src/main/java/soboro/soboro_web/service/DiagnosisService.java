package soboro.soboro_web.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.DiagnosisRecord;
import soboro.soboro_web.domain.enums.DiagnosisType;
import soboro.soboro_web.repository.DiagnosisRecordRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRecordRepository repository;

    public Mono<DiagnosisRecord> saveDiagnosis(String userId, DiagnosisType type, List<Integer> answers) {
        int score = answers.stream().mapToInt(Integer::intValue).sum();

        DiagnosisRecord record = DiagnosisRecord.builder()
                .userId(userId)
                .diagnosisType(type)
                .diagnosisScore(score)
                .diagnosisDate(LocalDate.now())
                .build();

        return repository.save(record);
    }

    public Flux<DiagnosisRecord> getUserDiagnosisHistory(String userId) {
        return repository.findAllByUserId(userId);
    }
}
