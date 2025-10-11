package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "like")
@CompoundIndex(name = "uniq_like_post_user", def = "{'postId': 1, 'userId': 1}", unique = true)//중복방지
public class Like {
    @Id
    private String likeId;
    private String userId;
    private String postId;

    // 커스텀 생성자
    public Like(String userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }

    @CreatedDate
    private Instant createdAt;
}
