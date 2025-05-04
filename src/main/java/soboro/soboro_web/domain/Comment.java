package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection="comment")
public class Comment {
    @Id
    private String commentId;
    private String userId;
    private String content;
    private LocalDateTime date = LocalDateTime.now();
}
