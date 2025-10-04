package soboro.soboro_web.domain;

import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InquiryComment {
    private String id;
    private String userId;
    private String content;
    private Instant date;
}
