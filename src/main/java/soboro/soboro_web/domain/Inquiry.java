package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Getter
@Setter
@Document(collection = "inquiry")
public class Inquiry extends Post {
    private String userId;
    private String password;
    private List<Comment> comments;
    private int isReply;    // 관리자가 답변했는지 유무
}
