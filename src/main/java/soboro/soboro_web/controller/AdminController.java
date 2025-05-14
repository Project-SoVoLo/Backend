package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Admin;
import soboro.soboro_web.jwt.JwtUtil;
import soboro.soboro_web.service.AdminService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 관리자 회원가입 + JWT 자동 발급(자동 로그인)
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody Admin admin){
        return adminService.register(admin)
                .map(savedAdmin -> {
                    String token = jwtUtil.generateToken(admin.getUserEmail(), "ADMIN");
                    long expiresAt = System.currentTimeMillis() + jwtUtil.getExpiration();

                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "관리자 회원가입 성공");
                    res.put("userEmail", savedAdmin.getUserEmail());
                    res.put("token", token);
                    res.put("expiresAt", expiresAt);
                    res.put("role", "ADMIN");
                    res.put("nextStep", "/admin/dashboard"); // 성공 시 관리자 대시보드로 이동

                    return ResponseEntity.ok(res);
                })
                .onErrorResume(error -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("message", error.getMessage());
                    res.put("nextStep", "/admin/register");
                    return Mono.just(ResponseEntity.ok(res));
                });
    }
    // 관리자 로그인
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Admin admin){
        return adminService.findByUserEmail(admin.getUserEmail())
                .filter(found -> passwordEncoder.matches(admin.getPassword(), found.getPassword()))
                .map(found -> {
                    String token = jwtUtil.generateToken(found.getUserEmail(), "ADMIN");
                    long expiresAt = System.currentTimeMillis() + jwtUtil.getExpiration();

                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "관리자 로그인 성공");
                    res.put("userEmail", found.getUserEmail());
                    res.put("token", token);
                    res.put("expiresAt", expiresAt);
                    res.put("role", "ADMIN");
                    res.put("nextStep", "/admin/dashboard");

                    return ResponseEntity.ok(res);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).body(
                        Map.of(
                            "message", "이메일 또는 비밀번호가 올바르지 않습니다.",
                            "nextStep", "/admin/login"
                        )
                )));
    }

    // 관리자만 접근 가능한 대시보드
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')") // 관리자 전용 API 보호
    public Mono<ResponseEntity<Map<String, String>>> dashboard(){
        Map<String, String> res = new HashMap<>();
         res.put("message", "관리자만 접근 가능한 대시보드입니다.");
        return Mono.just(ResponseEntity.ok(res));
    }
}
