package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.DiagnosisRequestDto;
import soboro.soboro_web.dto.DiagnosisResponseDto;
import soboro.soboro_web.service.DiagnosisService;

import java.security.Principal;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    //자가진단 결과 제출
    @PostMapping
    public Mono<ResponseEntity<DiagnosisResponseDto>> submitDiagnosis(
            Principal principal,
            @RequestBody DiagnosisRequestDto dto
    ) {
        String userId = principal.getName(); //JWT에서 사용자 ID 추출

        return diagnosisService.saveDiagnosis(userId, dto.getType(), dto.getAnswers())
                .map(record -> ResponseEntity.ok(new DiagnosisResponseDto(
                        record.getDiagnosisDate(),
                        record.getDiagnosisType(),
                        record.getDiagnosisScore()
                )));
    }

    //마이페이지 결과 조회
    @GetMapping("/history")
    public Flux<DiagnosisResponseDto> getMyDiagnosisHistory(Principal principal) {
        String userId = principal.getName();

        return diagnosisService.getUserDiagnosisHistory(userId)
                .map(record -> new DiagnosisResponseDto(
                        record.getDiagnosisDate(),
                        record.getDiagnosisType(),
                        record.getDiagnosisScore()
                ));
    }
}
