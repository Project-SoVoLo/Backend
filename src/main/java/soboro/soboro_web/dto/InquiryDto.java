package soboro.soboro_web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class InquiryDto {

    // 목록 조회 시 응답 아이템
    public record ListItem(
            String id,
            String title,
            String author,
            boolean isReply
    ) {}


    // 글 작성 요청
    public record CreateReq(
            @NotBlank String title,
            @NotBlank String content,
            @NotBlank String password
    ) {}

    // 글 작성 응답
    public record CreateRes(
            String inquiryId,
            String message
    ) {}


    // 글 삭제 요청
    public record DeleteReq(@NotBlank String password) {}
    // 글 삭제 시 응답
    public record MessageRes(String message) {}


    // 비밀번호 입력 후 열람 요청
    public record ReadReq(@NotBlank String password) {}

    // 열람 요청 시 포함된 댓글 응답
    public record CommentDto(
            String userId,
            String content,
            String date
    ) {}

    // 열람 요청 시 응답
    public record ReadRes(
            String inquiryId,
            String title,
            String content,
            List<CommentDto> comments
    ) {}


    // 댓글 작성 요청
    public record CommentCreateReq(
            @NotBlank String userId,
            @NotBlank String content
    ) {}

    // 댓글 작성 완료 응답
    public record CommentCreateRes(
            String commentId,
            String userId,
            String content,
            String date
    ) {}
}
