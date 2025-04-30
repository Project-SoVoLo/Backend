package soboro.soboro_web.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//
//@Getter
//@Setter
@Document(collection = "users")
public class User {
    @Id
    private String _id;         // DB에서 사용되는 기본 데이터의 id

    // 사용자 속성 정의
    private String userEmail;   // 로그인 시 사용하는 이메일
    private String password;    // 로그인 시 사용하는 비밀번호

    private String userName;
    private String nickname;
    private int userBirth;
    private String userGender;
    private int userPhone;

    // Getter & Setter

    public String get_id() {
        return _id;
    }

    public String getPassword() {
        return password;
    }
    public String getUserName() {
        return userName;
    }
    public String getNickname() {
        return nickname;
    }
    public int getUserBirth() {
        return userBirth;
    }
    public String getUserGender() {
        return userGender;
    }
    public int getUserPhone() {
        return userPhone;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void set_id(String _id) {
        this._id = _id;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setUserBirth(int userBirth) {
        this.userBirth = userBirth;
    }
    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }
    public void setUserPhone(int userPhone) {
        this.userPhone = userPhone;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

}