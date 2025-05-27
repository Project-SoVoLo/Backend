package soboro.soboro_web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatResponse {
    private String reply;   // 챗봇으로부터 받은 응답
}
