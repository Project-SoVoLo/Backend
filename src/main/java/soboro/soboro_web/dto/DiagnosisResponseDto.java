package soboro.soboro_web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soboro.soboro_web.domain.enums.DiagnosisType;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
//응답
public class DiagnosisResponseDto {
    private LocalDate diagnosisDate;
    private DiagnosisType diagnosisType;
    private int diagnosisScore;
}
