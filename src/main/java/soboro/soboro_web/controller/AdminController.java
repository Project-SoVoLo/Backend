package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.*;
import soboro.soboro_web.jwt.JwtUtil;
import soboro.soboro_web.service.AdminService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    // 관리자 회원가입 + JWT 자동 발급
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody AdminAuthRequest req) {
        return adminService.register(req)
                .map(savedAdmin -> {
                    String token = jwtUtil.generateToken(savedAdmin.getUserEmail(), "ADMIN");
                    long expiresAt = System.currentTimeMillis() + (jwtUtil.getExpiration() * 1000L);

                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "관리자 회원가입 성공");
                    res.put("userEmail", savedAdmin.getUserEmail());
                    res.put("token", token);
                    res.put("expiresAt", expiresAt);
                    res.put("role", "ADMIN");
                    res.put("nextStep", "/admin/dashboard");
                    return ResponseEntity.ok(res);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        Map.of("error", e.getMessage(), "nextStep", "/admin/register")
                )));
    }

    // 관리자 로그인 + JWT 자동 발급
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody AdminAuthRequest req) {
        return adminService.login(req)
                .map(foundAdmin -> {
                    String token = jwtUtil.generateToken(foundAdmin.getUserEmail(), "ADMIN");
                    long expiresAt = System.currentTimeMillis() + (jwtUtil.getExpiration() * 1000L);

                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "관리자 로그인 성공");
                    res.put("userEmail", foundAdmin.getUserEmail());
                    res.put("token", token);
                    res.put("expiresAt", expiresAt);
                    res.put("role", "ADMIN");
                    res.put("nextStep", "/admin/dashboard");
                    return ResponseEntity.ok(res);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).body(
                        Map.of("error", "이메일 또는 비밀번호가 올바르지 않습니다.", "nextStep", "/admin/login")
                )));
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<AdminUserListResponse>> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.listUsers(query, page, size).map(ResponseEntity::ok);
    }

    @GetMapping("/links")
    public Mono<ResponseEntity<AdminLinkResponse>> links() {
        return adminService.links().map(ResponseEntity::ok);
    }

    @GetMapping("/inquiry/comments")
    public Mono<ResponseEntity<AdminCommentFeedResponse>> recentComments(
            @RequestParam(required = false) String since,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return adminService.recentComments(since, limit).map(ResponseEntity::ok);
    }
}
