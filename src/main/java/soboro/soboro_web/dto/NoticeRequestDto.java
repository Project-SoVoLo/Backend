package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequestDto { // 공지사항 게시물 요청 Dto
    private String title;
    private String content;
}
