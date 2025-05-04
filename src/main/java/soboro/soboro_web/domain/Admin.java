package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection="admin")
public class Admin {
    @Id
    private String adminId;     // DB에서 사용되는 기본 데이터의 id

    // 사용자 속성 정의
    private String userEmail;   // 로그인 시 사용하는 이메일
    private String password;    // 로그인 시 사용하는 비밀번호

    private String userName;

    private List<Inquiry> incompletePost;   // 답변 안한 문의게시글 리스트
}
