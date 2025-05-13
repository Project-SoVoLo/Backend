package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    private String message; // 사용자가 입력하여 요청한 메세지
}
