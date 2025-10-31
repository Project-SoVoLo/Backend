package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection= "post")
public class Post {
    @Id
    private String postId;  //자동생성되는 _id
    private String title;
    private String content;
    private LocalDateTime date = LocalDateTime.now();
    private long likeCount;
    private long bookmarkCount;
}
