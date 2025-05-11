package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserExtraInfoRequest {
    // 카카오 회원가입 시, Email, Nickname 을 제외한 나머지 정보는 새로 입력받아야 하기 때문에 해당 dto 사용
    private String userEmail;
    private String userName;
    private String nickname;
    private int userBirth;
    private String userGender;
    private int userPhone;
}
