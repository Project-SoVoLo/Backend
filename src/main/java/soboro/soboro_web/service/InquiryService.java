package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import soboro.soboro_web.domain.Inquiry;
import soboro.soboro_web.domain.InquiryComment;
import soboro.soboro_web.dto.InquiryDto;
import soboro.soboro_web.repository.InquiryRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 전체 건의 게시글 조회
    public Flux<InquiryDto.ListItem> listAll() {
        return repo.findAll()
                .filter(i -> !i.isDeleted())
                .map(i -> new InquiryDto.ListItem(i.getId(), i.getTitle(), i.getAuthor(), !i.getComments().isEmpty()));
    }

    // 건의 게시글 작성하기
    public Mono<InquiryDto.CreateRes> create(InquiryDto.CreateReq req, String userId) {
        var now = Instant.now();
        var inquiry = Inquiry.builder()
                .title(req.title())
                .content(req.content())
                .author((userId == null || userId.isBlank()) ? "anonymous" : userId)
                .createdAt(now).updatedAt(now).deleted(false)
                .build();

        // BCrypt는 CPU바운드 → boundedElastic에서 해시
        return Mono.fromCallable(() -> encoder.encode(req.password()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(hash -> { inquiry.setPasswordHash(hash); return inquiry; })
                .flatMap(repo::save)
                .map(saved -> new InquiryDto.CreateRes(saved.getId(), "건의사항이 등록되었습니다."));
    }

    // 건의 게시글 삭제하기
    public Mono<InquiryDto.MessageRes> delete(String id, InquiryDto.DeleteReq req) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(i -> checkPassword(i, req.password())
                        .then(Mono.defer(() -> {
                            i.setDeleted(true);
                            i.setUpdatedAt(Instant.now());
                            return repo.save(i);
                        })))
                .thenReturn(new InquiryDto.MessageRes("삭제가 완료되었습니다."));
    }

    // 비번 검증 + 본문/댓글 반환
    public Mono<InquiryDto.ReadRes> readWithPassword(String id, InquiryDto.ReadReq req) {
        return repo.findById(id)
                .filter(i -> !i.isDeleted())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(i -> checkPassword(i, req.password()).thenReturn(i))
                .map(i -> new InquiryDto.ReadRes(
                        i.getId(), i.getTitle(), i.getContent(),
                        i.getComments().stream()
                                .map(c -> new InquiryDto.CommentDto(c.getUserId(), c.getContent(), c.getDate().toString()))
                                .toList()
                ));
    }

    // 댓글 작성하기
    public Mono<InquiryDto.CommentCreateRes> addComment(String id, InquiryDto.CommentCreateReq req) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(i -> {
                    boolean isAdmin = "admin".equalsIgnoreCase(req.userId());
                    boolean isAuthor = req.userId().equals(i.getAuthor());
                    if (!isAdmin && !isAuthor) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed"));
                    }
                    var now = Instant.now();
                    var cmtId = UUID.randomUUID().toString();
                    var comment = InquiryComment.builder()
                            .id(cmtId).userId(req.userId()).content(req.content()).date(now).build();
                    i.getComments().add(comment);
                    i.setUpdatedAt(now);
                    return repo.save(i).thenReturn(new InquiryDto.CommentCreateRes(cmtId, req.userId(), req.content(), now.toString()));
                });
    }

    private Mono<Void> checkPassword(Inquiry i, String raw) {
        return Mono.fromCallable(() -> encoder.matches(raw, i.getPasswordHash()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(ok -> ok ? Mono.<Void>empty()
                        : Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid password")));
    }

}
