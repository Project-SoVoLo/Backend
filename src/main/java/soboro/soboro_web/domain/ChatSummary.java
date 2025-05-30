package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import soboro.soboro_web.domain.enums.EmotionTypes;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "chat_summary")
public class ChatSummary {

    @Id
    private String summaryId;
    private String userEmail; // 사용자와 연결

    @Field("date")
    private LocalDate date; // 대화가 끝난 날짜
    private String summary; // 대화 요약
    private String feedback; // 챗봇 피드백 요약
    private EmotionTypes emotionType; // 긍정 / 중립 / 부정 (enum)

}
