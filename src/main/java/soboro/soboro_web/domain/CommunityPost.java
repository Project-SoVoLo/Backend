package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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

    private List<Comment> comments;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();//수정할때 시간 업데이트
}
