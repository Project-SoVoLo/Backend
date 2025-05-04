package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "community")
public class Community extends Post{
    @Id
    private String userId;
    private List<Like> likes;
    private List<Comment> comments;
    private List<Bookmark> bookmarks;
    private byte[] images;
}
