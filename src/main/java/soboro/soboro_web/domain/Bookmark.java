package soboro.soboro_web.domain;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookmark")
public class Bookmark {
    @Id
    private String bookmarkId;
    private String userId;
    private String postId;

    private String postType;


    public Bookmark(String userId, String postId) {
        this.userId = userId;
        this.postId = postId;
    }
}