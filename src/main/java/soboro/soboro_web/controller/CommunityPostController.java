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

    @GetMapping
    public Flux<CommunityResponseDto> getAll(Authentication authentication) {
        String loginEmailOrNull = (authentication != null) ? authentication.getName() : null;
        return service.getAll(loginEmailOrNull);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<CommunityResponseDto>> getOne(
            @PathVariable String id,
            Authentication authentication) {

        String loginEmailOrNull = (authentication != null) ? authentication.getName() : null;
        return service.getById(id, loginEmailOrNull)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<CommunityResponseDto>> create(
            @Valid @RequestBody CommunityRequestDto body,
            Authentication authentication
    ) {
        String email = requireEmail(authentication);
        return service.create(body, email)
                .map(res -> ResponseEntity.created(URI.create("/api/community-posts/" + res.getId())).body(res));
    }

    // 수정
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CommunityResponseDto>> update(
            @PathVariable String id,
            @Valid @RequestBody CommunityRequestDto body,
            Authentication authentication
    ) {
        String email = requireEmail(authentication);
        return service.update(id, body, email)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalAccessException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 삭제
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id,
                                             Authentication authentication) {
        String email = requireEmail(authentication);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        return service.delete(id, email, isAdmin)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(IllegalAccessException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build()));
    }

    // 좋아요
    @PostMapping("/{id}/like")
    public Mono<ResponseEntity<Boolean>> toggleLike(@PathVariable String id,
                                                    Authentication authentication) {
        String email = requireEmail(authentication);
        return service.toggleLikeByEmail(email, id).map(ResponseEntity::ok);
    }

    // 북마크
    @PostMapping("/{id}/bookmark")
    public Mono<ResponseEntity<Boolean>> toggleBookmark(@PathVariable String id,
                                                        Authentication authentication) {
        String email = requireEmail(authentication);
        return service.toggleBookmarkByEmail(email, id).map(ResponseEntity::ok);
    }

    // 댓글
    @GetMapping("/{postId}/comments")
    public Flux<CommentDto.Item> listComments(@PathVariable String postId) {
        return service.listComments(postId);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentDto.Item> createComment(@PathVariable String postId,
                                               Authentication authentication,
                                               @Valid @RequestBody CommentDto.CreateReq req) {
        String email = requireEmail(authentication);
        return service.addComment(postId, email, req);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public Mono<CommentDto.Item> updateComment(@PathVariable String postId,
                                               @PathVariable String commentId,
                                               Authentication authentication,
                                               @Valid @RequestBody CommentDto.UpdateReq req) {
        String email = requireEmail(authentication);
        return service.updateComment(commentId, email, req);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public Mono<ResponseEntity<CommentDto.MessageRes>> deleteComment(@PathVariable String postId,
                                                                     @PathVariable String commentId,
                                                                     Authentication authentication) {
        String email = requireEmail(authentication);
        boolean isAdmin = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return service.deleteComment(commentId, email, isAdmin)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalAccessException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()));
    }


    private String requireEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return authentication.getName(); // principal=email
    }
}
