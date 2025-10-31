package soboro.soboro_web.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CardResponseDto {
    private String postId;
    private String title;
    private String content;
    private LocalDateTime date;
    private String adminId;
    private List<String> imageUrls;
    private String thumbnailUrl;

    private boolean bookmarked; // 로그인 사용자가 북마크했는지 여부
}