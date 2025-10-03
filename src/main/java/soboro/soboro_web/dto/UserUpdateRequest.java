package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserUpdateRequest {
    private String newEmail;    // 바꿀 이메일
    private String newPassword; // 새 비번
    private String userName;    // 이름
    private String nickname;    // 닉네임
    private String userPhone;   // 전화번호
}
