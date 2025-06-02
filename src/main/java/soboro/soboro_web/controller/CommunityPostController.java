package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.CommunityPost;
import soboro.soboro_web.service.CommunityPostService;

@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService service;

    @PostMapping//게시글 작성
    public Mono<ResponseEntity<CommunityPost>> create(@RequestBody CommunityPost post) {
        return service.create(post).map(ResponseEntity::ok);
    }

    @GetMapping//전체 게시글 목록 조회
    public Flux<CommunityPost> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")//게시글 조회
    public Mono<ResponseEntity<CommunityPost>> getOne(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")//게시글 수정
    public Mono<ResponseEntity<CommunityPost>> update(@PathVariable String id, @RequestBody CommunityPost post) {
        return service.update(id, post)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")//게시글 삭제
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return service.delete(id).thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
