package soboro.soboro_web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminCommentFeedResponse {
    private List<CommentItem> items;

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CommentItem {
        private String commentId;
        private String inquiryId;
        private String title;
        private String author;
        private Instant createdAt;
        private String inquiryPostUrl;
        private String excerpt;  // content 일부분
    }
}
