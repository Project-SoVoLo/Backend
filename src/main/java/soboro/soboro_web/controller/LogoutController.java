package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/logout")
@RequiredArgsConstructor
public class LogoutController {

    @PostMapping
    public Mono<ResponseEntity<String>> logout() {
        return Mono.just(ResponseEntity.ok("로그아웃 되었습니다"));
    }
}
