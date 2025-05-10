package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import soboro.soboro_web.domain.enums.EmotionTypes;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String userId;         // DB에서 사용되는 기본 데이터의 id

    // 사용자 속성 정의
    private String userEmail;   // 로그인 시 사용하는 이메일
    private String password;    // 로그인 시 사용하는 비밀번호

    private String userName;
    private String nickname;
    private int userBirth;
    private String userGender;
    private int userPhone;

    private EmotionTypes emotionStatus;     // 현재 사용자의 감정 상태 (긍정, 중립, 부정)

    public User() {}
}