package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import soboro.soboro_web.domain.enums.DiagnosisType;

@Getter
@Setter
@Document(collection = "diagnosis_questions")
public class DiagnosisQuestion {
    @Id
    private String id;

    private DiagnosisType type;
    private int number; // 1~9
    private String questionText;
}
