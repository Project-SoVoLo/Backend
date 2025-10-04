package soboro.soboro_web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.InquiryDto;
import soboro.soboro_web.service.InquiryService;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService service;

    @GetMapping("/all")
    public Flux<InquiryDto.ListItem> listAll() {
        return service.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<InquiryDto.CreateRes> create(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                             @Valid @RequestBody InquiryDto.CreateReq req) {
        return service.create(req, userId);
    }

    @DeleteMapping("/{inquiryId}")
    public Mono<InquiryDto.MessageRes> delete(@PathVariable String inquiryId,
                                              @Valid @RequestBody InquiryDto.DeleteReq req) {
        return service.delete(inquiryId, req);
    }

    @PostMapping("/{inquiryId}/read")
    public Mono<InquiryDto.ReadRes> read(@PathVariable String inquiryId,
                                         @Valid @RequestBody InquiryDto.ReadReq req) {
        return service.readWithPassword(inquiryId, req);
    }

    @PostMapping("/{inquiryId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<InquiryDto.CommentCreateRes> comment(@PathVariable String inquiryId,
                                                     @Valid @RequestBody InquiryDto.CommentCreateReq req) {
        return service.addComment(inquiryId, req);
    }
}
