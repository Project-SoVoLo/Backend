package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "like")
public class Like {
    @Id
    private String likeId;  // 자동생성된 _id
    private String userId;
    private String postId;

    // 커스텀 생성자
    public Like(String userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
