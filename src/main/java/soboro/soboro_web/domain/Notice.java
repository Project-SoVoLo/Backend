package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Getter
@Setter
@Document(collection="notice")
public class Notice extends Post {
    @Id
    private String adminId;
    private List<Like> likes;
}
