package soboro.soboro_web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class CommentDto {

    // 작성
    public record CreateReq(@NotBlank String content) {}

    // 수정
    public record UpdateReq(@NotBlank String content) {}

    // 목록
    public record Item(
            String commentId,
            String postId,
            String nickname,
            String content,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record MessageRes(String message) {}
}
