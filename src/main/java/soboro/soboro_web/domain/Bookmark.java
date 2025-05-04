package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "bookmark")
public class Bookmark {
    @Id
    private String bookmarkId;
    private String userId;
    private String postId;
}
