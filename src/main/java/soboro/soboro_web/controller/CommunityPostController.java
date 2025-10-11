package soboro.soboro_web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.CommunityRequestDto;
import soboro.soboro_web.dto.CommunityResponseDto;
import soboro.soboro_web.service.CommunityPostService;
import soboro.soboro_web.dto.CommentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;


import java.net.URI;

@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService service;

    //조회

    @GetMapping
    public Flux<CommunityResponseDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<CommunityResponseDto>> getOne(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //생성

    @PostMapping
    public Mono<ResponseEntity<CommunityResponseDto>> create(
            @Valid @RequestBody CommunityRequestDto body,
            Authentication authentication
    ) {
        String userKey = requireUser(authentication);
        return service.create(body, userKey)
                .map(res -> ResponseEntity
                        .created(URI.create("/api/community-posts/" + res.getId()))
                        .body(res));
    }

    //수정
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CommunityResponseDto>> update(
            @PathVariable String id,
            @Valid @RequestBody CommunityRequestDto body,
            Authentication authentication
    ) {
        String userKey = requireUser(authentication);
        return service.update(id, body, userKey)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalAccessException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    //삭제
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id,
                                             Authentication authentication) {
        String userKey = requireUser(authentication);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())); // ✅ 관리자 여부

        return service.delete(id, userKey, isAdmin) // ✅ 시그니처 변경
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(IllegalAccessException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build()));
    }



    //좋아요
    @PostMapping("/{id}/likes")
    public Mono<ResponseEntity<Boolean>> toggleLike(@PathVariable String id,
                                                    Authentication authentication) {
        String userKey = requireUser(authentication);
        return service.toggleLike(userKey, id).map(ResponseEntity::ok);
    }
    //북마크
    @PostMapping("/{id}/bookmarks")
    public Mono<ResponseEntity<Boolean>> toggleBookmark(@PathVariable String id,
                                                        Authentication authentication) {
        String userKey = requireUser(authentication);
        return service.toggleBookmark(userKey, id).map(ResponseEntity::ok);
    }

    //댓글
    @GetMapping("/{postId}/comments")
    public Flux<CommentDto.Item> listComments(@PathVariable String postId) {
        return service.listComments(postId);
    }

    //작성
    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentDto.Item> createComment(@PathVariable String postId,
                                               Authentication authentication,
                                               @Valid @RequestBody CommentDto.CreateReq req) {
        String email = requireUser(authentication);
        return service.addComment(postId, email, req);
    }

    //수정
    @PutMapping("/{postId}/comments/{commentId}")
    public Mono<CommentDto.Item> updateComment(@PathVariable String postId,
                                               @PathVariable String commentId,
                                               Authentication authentication,
                                               @Valid @RequestBody CommentDto.UpdateReq req) {
        String email = requireUser(authentication);
        return service.updateComment(commentId, email, req);
    }

    //삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    public Mono<ResponseEntity<CommentDto.MessageRes>> deleteComment(@PathVariable String postId,
                                                                     @PathVariable String commentId,
                                                                     Authentication authentication) {
        String email = requireUser(authentication);
        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return service.deleteComment(commentId, email, isAdmin)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalAccessException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()));
    }

    //헬퍼

    private String requireUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return authentication.getName();
    }
}
