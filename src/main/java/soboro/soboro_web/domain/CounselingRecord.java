package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import soboro.soboro_web.domain.enums.EmotionTypes;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "counseling")
public class CounselingRecord {
    @Id
    private String counselingId;
    private String userId;

    private LocalDate counselingDate;   // 상담 일시
    private String counselingSummary;     // 상담 내역 요약본 저장
    private String counselingFeedback;    // 상담 후 피드백 저장

    public CounselingRecord() {
    }
}
