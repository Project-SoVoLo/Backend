package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.AdminAuthRequest;
import soboro.soboro_web.dto.AdminCommentFeedResponse;
import soboro.soboro_web.dto.AdminLinkResponse;
import soboro.soboro_web.dto.AdminResponse;
import soboro.soboro_web.dto.AdminUserListResponse;
import soboro.soboro_web.service.AdminService;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public Mono<ResponseEntity<AdminResponse>> register(@RequestBody AdminAuthRequest req) {
        return adminService.register(req)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        AdminResponse.builder()
                                .role("ERROR")
                                .userEmail(req.getUserEmail())
                                .build()
                )));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AdminResponse>> login(@RequestBody AdminAuthRequest req) {
        return adminService.login(req)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).body(
                        AdminResponse.builder()
                                .role("UNAUTHORIZED")
                                .userEmail(req.getUserEmail())
                                .build()
                )));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<AdminUserListResponse>> listUsers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.listUsers(query, page, size)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/links")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<AdminLinkResponse>> links() {
        return adminService.links()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/inquiry/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<AdminCommentFeedResponse>> recentComments(
            @RequestParam(required = false) String since,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return adminService.recentComments(since, limit)
                .map(ResponseEntity::ok);
    }
}
