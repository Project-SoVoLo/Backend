package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostBlock {
    private String type;    // 텍스트 or 이미지
    private String content; // 텍스트 or 이미지 URL
}
