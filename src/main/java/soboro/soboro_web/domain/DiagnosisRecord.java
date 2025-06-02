package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import soboro.soboro_web.domain.enums.DiagnosisType;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "diagnosis")
public class DiagnosisRecord {
    @Id
    private String diagnosisId;


    // 자가진단 속성 정의
    private LocalDate diagnosisDate;    // 자가진단 한 날짜
    private DiagnosisType diagnosisType;   // 자가진단 종류 (우울증, 불안, 조기정신증, 조울증, 스트레스, 불면, 알코올중독, 스마트기기사용장애)

    private int diagnosisScore;     // 자가진단 완료 시 점수 기록
    private String userId;



}

