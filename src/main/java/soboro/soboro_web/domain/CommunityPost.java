package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "community_posts")
public class CommunityPost {
    @Id
    private String id;

    private String title;
    private String userId;
    private String nickname;
    private List<PostBlock> blocks;

    private long likeCount;
    private long bookmarkCount;
    private long commentCount;

    // 최신순 정렬
    @Indexed(direction = IndexDirection.DESCENDING)
    private Instant createdAt;

    private Instant updatedAt;
}
