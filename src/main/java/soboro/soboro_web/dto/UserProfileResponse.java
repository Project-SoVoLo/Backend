package soboro.soboro_web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private String userEmail;
    private String nickname;
    private String userName;
    private String userPhone;
    private String userBirth;
    private String userGender;
}
