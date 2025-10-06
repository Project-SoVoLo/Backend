package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;
import soboro.soboro_web.domain.Like;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NoticeResponseDto { // 공지사항 게시물 응답 Dto
    private String postId;
    private String title;
    private String content;
    private LocalDateTime date;
    private String adminId;
    private int likeCount; // 게시물 좋아요 수
    private boolean liked; // 현재 유저가 좋아요 눌렀는지 여부
}
