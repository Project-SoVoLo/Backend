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
import soboro.soboro_web.domain.PostBlock;
import soboro.soboro_web.service.S3Service;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import java.net.URI;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/community-posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService service;
    private final S3Service s3Service;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CommunityResponseDto>> create(
            @RequestPart("title") String title,
            @RequestPart("blocks") String blocksJson,
            @RequestPart(value = "images", required = false) Flux<FilePart> images,
            Authentication authentication
    ) {
        String email = requireEmail(authentication);

        Mono<List<String>> imageUrlsMono = (images == null ? Flux.<FilePart>empty() : images)
                .flatMap(file -> s3Service.uploadFile(file, "community/images"))
                .collectList();

        return imageUrlsMono.flatMap(urls -> {
            List<PostBlock> blocks;
            try {
                blocks = new ObjectMapper().readValue(
                        blocksJson, new TypeReference<List<PostBlock>>() {});
            } catch (Exception e) {
                return Mono.error(new IllegalArgumentException("blocks JSON 파싱 실패", e));
            }

            // 빈 image 블록에 업로드한 URL 주입 (image 블록 순서대로)
            int idx = 0;
            for (PostBlock b : blocks) {
                if ("image".equalsIgnoreCase(b.getType())
                        && (b.getUrl() == null || b.getUrl().isBlank())) {
                    if (idx < urls.size()) {
                        b.setUrl(urls.get(idx++));
                    }
                }
            }

            CommunityRequestDto dto = new CommunityRequestDto();
            dto.setTitle(title);
            dto.setBlocks(blocks);

            return service.create(dto, email);
        }).map(res -> ResponseEntity
                .created(URI.create("/api/community-posts/" + res.getId()))
                .body(res));
    }

    // 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<CommunityResponseDto>> update(
            @PathVariable String id,
            @RequestPart("title") String title,
            @RequestPart("blocks") String blocksJson,
            @RequestPart(value = "images", required = false) Flux<FilePart> images,
            Authentication authentication
    ) {
        String email = requireEmail(authentication);

        Mono<List<String>> imageUrlsMono = (images == null ? Flux.<FilePart>empty() : images)
                .flatMap(file -> s3Service.uploadFile(file, "community/images"))
                .collectList();

        return imageUrlsMono.flatMap(urls -> {

            List<PostBlock> blocks;
            try {
                blocks = new ObjectMapper().readValue(blocksJson, new TypeReference<List<PostBlock>>() {});
            } catch (Exception e) {
                return Mono.error(new IllegalArgumentException("blocks JSON 파싱 실패", e));
            }

            int i = 0;
            for (PostBlock b : blocks) {
                if ("image".equalsIgnoreCase(b.getType())
                        && (b.getUrl() == null || b.getUrl().isBlank())
                        && i < urls.size()) {
                    b.setUrl(urls.get(i++));
                }
            }

            CommunityRequestDto dto = new CommunityRequestDto();
            dto.setTitle(title);
            dto.setBlocks(blocks);

            return service.update(id, dto, email);
        }).map(ResponseEntity::ok);
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
