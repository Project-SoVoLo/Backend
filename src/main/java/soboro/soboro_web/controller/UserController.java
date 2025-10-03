package soboro.soboro_web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.dto.UserExtraInfoRequest;
import soboro.soboro_web.dto.UserUpdateRequest;
import soboro.soboro_web.repository.UserRepository;
import soboro.soboro_web.jwt.JwtUtil;
import soboro.soboro_web.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 이메일 회원가입 + JWT 자동 발급(자동 로그인)
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody User user) {
        return userService.register(user)
                .map(savedUser -> {
                    String token = jwtUtil.generateToken(savedUser.getUserEmail(), "USER");
                    long expiresAt = System.currentTimeMillis() + jwtUtil.getExpiration();

                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "사용자 회원가입 성공");
                    res.put("userEmail", savedUser.getUserEmail());
                    res.put("token", token);
                    res.put("expiresAt", expiresAt);
                    res.put("role", "USER");
                    res.put("nextStep", "/home");

                    return ResponseEntity.ok(res);
                })
                .onErrorResume(error -> {
                    // 예외 발생 시 400 에러 + 메시지 JSON 형태로 응답
                    Map<String, Object> res = new HashMap<>();
                    res.put("error", error.getMessage());
                    res.put("nextStep", "/register");
                    return Mono.just(ResponseEntity.badRequest().body(res));
                });
    }

    // 로그인
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody User user){
        return userService.findByUserEmail(user.getUserEmail())
                .filter(found -> passwordEncoder.matches(user.getPassword(), found.getPassword()))
                .map(found -> {
                    String token = jwtUtil.generateToken(found.getUserEmail(), "USER");
                    long expiresAt = System.currentTimeMillis() + jwtUtil.getExpiration();

                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "사용자 로그인 성공");
                    res.put("userEmail", found.getUserEmail());
                    res.put("token", token);
                    res.put("expiresAt", expiresAt);
                    res.put("role", "USER");
                    res.put("nextStep", "/home");

                    return ResponseEntity.ok(res);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).body(
                        Map.of(
                                "message", "이메일 또는 비밀번호가 올바르지 않습니다.",
                                "nextStep", "/login"
                        )
                )));
    }

    // 카카오 로그인 시 추가 정보 입력받아서 회원가입 완료하기
    @PostMapping("/update-info")
    public Mono<ResponseEntity<String>> updateUserExtraInfo(@RequestBody UserExtraInfoRequest request) {
        return userRepository.findByUserEmail(request.getUserEmail())
                .flatMap(user -> {
                    if (request.getUserName() != null) user.setUserName(request.getUserName());
                    if (request.getNickname() != null) user.setNickname(request.getNickname());
                    if (request.getUserBirth() != null) user.setUserBirth(request.getUserBirth());
                    if (request.getUserGender() != null) user.setUserGender(request.getUserGender());
                    if (request.getUserPhone() != null) user.setUserPhone(request.getUserPhone());
                    return userRepository.save(user);
                })
                .map(savedUser -> ResponseEntity.ok("추가 정보 저장 완료"))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("사용자 정보를 찾을 수 없음")));
    }

    // 개인정보 수정 (JWT 기반)
    @PostMapping("/edit-info")
    public Mono<ResponseEntity<Map<String, Object>>> editUserInfo(
            @RequestBody UserUpdateRequest request,
            ServerHttpRequest httpRequest
    ){
        // Authorization 헤더에서 JWT 꺼내기
        String token = httpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(token == null || !token.startsWith("Bearer ")){
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰이 없습니다.")));
        }
        token = token.substring(7); // "Bearer " 제거

        // 토큰에서 사용자 이메일 추출
        String email;
        try{
            email = jwtUtil.getUsernameFromToken(token);
        }catch(Exception e) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰이 없습니다.")));
        }

        return userRepository.findByUserEmail(email)
                .flatMap(user ->{
                    // 새 이메일(중복 체크는 따로 서비스에서)
                    if(request.getNewEmail() != null){
                        user.setUserEmail(request.getNewEmail());
                    }
                    // 이름 변경
                    if(request.getUserName() != null){
                        user.setUserName(request.getUserName());
                    }
                    // 닉네임 변경
                    if(request.getNickname() != null){
                        user.setNickname(request.getNickname());
                    }
                    // 전화번호 변경
                    if(request.getUserPhone() != null){
                        user.setUserPhone(request.getUserPhone());
                    }
                    // 비밀번호 변경(암호화 필수)
                    if(request.getNewPassword() != null){
                        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    }

                    return userRepository.save(user);
                })
                .map(updatedUser -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "개인정보 수정 완료");
                    res.put("userEmail", updatedUser.getUserEmail());
                    res.put("nickname", updatedUser.getNickname());
                    res.put("nextStep", "/api/mypage/profile"); // 완료 후 마이페이지의 프로필 화면으로
                    return ResponseEntity.ok(res);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "사용자를 찾을 수 없습니다."))));
    }
}