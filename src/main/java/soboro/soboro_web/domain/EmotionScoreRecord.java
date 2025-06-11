package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import soboro.soboro_web.domain.enums.EmotionTypes;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "emotion")
public class EmotionScoreRecord {
    @Id
    private String emotionId;
    //    private String userId;
    private String userEmail;

    private LocalDate emotionDate;  // 감정 점수가 기록된 날짜
    private int phqScore;           // phq-9 점수 기록
    private String googleEmotion;      // google NLP 클래스 기록 (긍.중.부)
    private EmotionTypes emotionType;        // 긍정,중립,부정 상태 기록
    private String summary;     // gemini 요약 결과
    private String feedback;    // 챗봇 피드백

    public EmotionScoreRecord() {
    }
}