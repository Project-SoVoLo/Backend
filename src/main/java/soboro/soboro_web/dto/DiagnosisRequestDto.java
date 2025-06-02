package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;
import soboro.soboro_web.domain.enums.DiagnosisType;

import java.util.List;

@Getter
@Setter
//요청
public class DiagnosisRequestDto {
    private DiagnosisType type;
    private List<Integer> answers;      //질문 9개에 대한 점수
}

