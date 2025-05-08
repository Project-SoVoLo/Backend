package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection="card")
public class Card extends Post {
    private String adminId;
    private byte[] image;
    private List<Bookmark> bookmarks;
}
