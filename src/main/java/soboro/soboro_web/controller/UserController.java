package soboro.soboro_web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.dto.UserExtraInfoRequest;
import soboro.soboro_web.repository.UserRepository;
import soboro.soboro_web.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository){
        this.userService = userService;
        this.userRepository = userRepository;
    }

    //
    @PostMapping("/register")
    public Mono<String> register(@RequestBody User user) {
        return userService.register(user)
                .map(saved -> "회원가입 완료: " + saved.getUserEmail());
    }

    // 카카오 로그인 시 추가 정보 입력받아서 회원가입 완료하기
    @PostMapping("/update-info")
    public Mono<ResponseEntity<String>> updateUserExtraInfo(@RequestBody UserExtraInfoRequest request) {
        return userRepository.findByUserEmail(request.getUserEmail())
                .flatMap(user -> {
                    user.setUserName(request.getUserName());
                    user.setNickname(request.getNickname());
                    user.setUserBirth(request.getUserBirth());
                    user.setUserGender(request.getUserGender());
                    user.setUserPhone(String.valueOf(request.getUserPhone()));
                    return userRepository.save(user);
                })
                .map(savedUser -> ResponseEntity.ok("추가 정보 저장 완료"))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("사용자 정보를 찾을 수 없음")));
    }

}
