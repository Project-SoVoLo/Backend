package soboro.soboro_web.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "inquiry")
@NoArgsConstructor @AllArgsConstructor @Builder
public class Inquiry{
    // 건의사항 게시판은 Post를 상속하지 않고 독립형 구조로 진행함 (제목, 본문, 비밀번호)

    @Id
    private String id;

    private String title;
    private String content;
    private String author;         // X-User-Id 헤더(없으면 "anonymous")
    private String passwordHash;   // BCrypt 등 해시로 저장

    @Builder.Default
    private List<InquiryComment> comments = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    private boolean deleted;
}