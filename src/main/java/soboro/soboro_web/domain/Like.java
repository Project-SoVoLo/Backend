package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collation = "like")
public class Like {
    @Id
    private String likeId;  // 자동생성된 _id
    private String userId;
    private String postId;
}
