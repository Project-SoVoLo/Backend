package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;
import soboro.soboro_web.domain.enums.EmotionTypes;

@Getter
@Setter
public class ChatSummaryRequest {
    private String chatLog; // 사용자와 챗봇의 전체 대화 내용
}
