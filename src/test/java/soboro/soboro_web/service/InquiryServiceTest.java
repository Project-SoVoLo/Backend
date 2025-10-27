package soboro.soboro_web.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import soboro.soboro_web.domain.Inquiry;
import soboro.soboro_web.domain.InquiryComment;
import soboro.soboro_web.dto.InquiryDto;
import soboro.soboro_web.repository.InquiryRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InquiryServiceTest {

    private final InquiryRepository repo = mock(InquiryRepository.class);
    private final InquiryService service = new InquiryService(repo);

    @Test
    void readWithPasswordReturnsCommentIncludingUserName() {
        var encoder = new BCryptPasswordEncoder();
        var password = "secure-password";
        var comment = InquiryComment.builder()
                .id(UUID.randomUUID().toString())
                .userId("author1")
                .userName("작성자1")
                .content("test comment")
                .date(Instant.now())
                .build();

        var inquiry = Inquiry.builder()
                .id("inq-1")
                .title("title")
                .content("content")
                .author("author1")
                .passwordHash(encoder.encode(password))
                .comments(new ArrayList<>(List.of(comment)))
                .deleted(false)
                .build();

        when(repo.findById(inquiry.getId())).thenReturn(Mono.just(inquiry));

        StepVerifier.create(service.readWithPassword(inquiry.getId(), new InquiryDto.ReadReq(password)))
                .assertNext(res -> {
                    assertEquals(1, res.comments().size());
                    var firstComment = res.comments().get(0);
                    assertEquals(comment.getUserId(), firstComment.userId());
                    assertEquals(comment.getUserName(), firstComment.userName());
                    assertEquals(comment.getContent(), firstComment.content());
                })
                .verifyComplete();

        verify(repo).findById(inquiry.getId());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void addCommentPersistUserName() {
        var inquiry = Inquiry.builder()
                .id("inq-2")
                .author("author2")
                .comments(new ArrayList<>())
                .deleted(false)
                .build();

        when(repo.findById(inquiry.getId())).thenReturn(Mono.just(inquiry));
        when(repo.save(any(Inquiry.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var req = new InquiryDto.CommentCreateReq("author2", "작성자2", "반갑습니다.");

        StepVerifier.create(service.addComment(inquiry.getId(), req))
                .assertNext(res -> {
                    assertEquals(req.userId(), res.userId());
                    assertEquals(req.userName(), res.userName());
                    assertEquals(req.content(), res.content());
                })
                .verifyComplete();

        assertEquals(1, inquiry.getComments().size());
        var storedComment = inquiry.getComments().get(0);
        assertEquals(req.userId(), storedComment.getUserId());
        assertEquals(req.userName(), storedComment.getUserName());
        assertEquals(req.content(), storedComment.getContent());

        var captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(repo).findById(inquiry.getId());
        verify(repo).save(captor.capture());
        verifyNoMoreInteractions(repo);

        var saved = captor.getValue();
        assertEquals(1, saved.getComments().size());
        assertEquals(req.userName(), saved.getComments().get(0).getUserName());
    }
}
