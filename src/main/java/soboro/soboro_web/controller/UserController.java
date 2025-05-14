package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.jwt.JwtUtil;
import soboro.soboro_web.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
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

}
