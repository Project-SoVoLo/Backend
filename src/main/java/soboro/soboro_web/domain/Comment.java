package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection="comment")
@CompoundIndex(name="idx_post_created", def="{ 'postId': 1, 'createdAt': -1 }")
public class Comment {
    @Id
    private String commentId;

    private String userId;
    private String postId;
    private String content;
    private String nickname;

    private Instant createdAt;
    private Instant updatedAt;
}
