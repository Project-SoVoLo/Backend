package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.dto.ChatSummaryRequest;
import soboro.soboro_web.dto.ChatSummaryResponse;
import soboro.soboro_web.dto.UserProfileResponse;
import soboro.soboro_web.repository.UserRepository;
import soboro.soboro_web.service.ChatSummaryService;
import soboro.soboro_web.service.MyPageService;
import soboro.soboro_web.dto.CommunityResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final ChatSummaryService chatSummaryService;
    private final MyPageService myPageService;
    private final UserRepository userRepository;

    // 마이페이지 메인 조회
    @GetMapping
    public Mono<ResponseEntity<String>> getMyPage() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    System.out.println("🔐 Authentication: " + ctx.getAuthentication());
                    return ctx.getAuthentication().getName();
                })
                .map(userEmail -> {
                    // 여기서는 단순히 마이페이지 접근 확인용
                    return ResponseEntity.ok("마이페이지 화면 - 사용자: " + userEmail);
                });
    }

    // 프로필 조회
    @GetMapping("/profile")
    public Mono<ResponseEntity<UserProfileResponse>> getMyProfile(){
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(userEmail ->
                        userRepository.findByUserEmail(userEmail)
                                .map(user -> ResponseEntity.ok(
                                        new UserProfileResponse(
                                                user.getUserEmail(),
                                                user.getNickname(),
                                                user.getUserName(),
                                                user.getUserPhone(),
                                                user.getUserBirth(),
                                                user.getUserGender()
                                        )
                                ))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // 챗봇 상담 내역 요약 저장
    @PostMapping("/chat-summaries")
    public Mono<ResponseEntity<Void>> saveChatSummary(@RequestBody ChatSummaryRequest request){

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(userEmail -> {
                    // 디버깅용 로그
                    System.out.println("✅ [컨트롤러] 요청 들어옴");
                    System.out.println("🔐 사용자 이메일: " + userEmail);

                    return chatSummaryService.summarizeAndSave(
                            userEmail,
                            request.getChatLog()
                    ).thenReturn(ResponseEntity.ok().build());
                });
    }

    // 챗봇 상담 내역 요약 조회 + 점수까지 같이 조회
    @GetMapping("/chat-summaries")
    public Flux<ChatSummaryResponse> getChatSummaries() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    String email = ctx.getAuthentication().getName();   // 디버깅용 - 현재 로그인된 사용자 조회 (이메일로 찍히는지)
                    System.out.println("현재 사용자 이메일: "+ email);
                    return email;
                })
                .flatMapMany(userEmail ->
                        chatSummaryService.getEmotionRecords(userEmail)
                                .flatMap(chatSummary ->
                                        chatSummaryService.getEmotionRecords(userEmail)
                                                .map(record -> new ChatSummaryResponse(
                                                        record.getEmotionDate(),
                                                        record.getSummary(),
                                                        record.getFeedback(),
                                                        record.getEmotionType().getKorean(),
                                                        record.getEmotionType().getColorCode(),
                                                        record.getPhqScore(),
                                                        record.getGoogleEmotion()
                                                ))));
    }

    @GetMapping("/bookmarks")
    public Flux<Object> getMyBookmarks() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMapMany(userId -> myPageService.getAllBookmarks(userId));
    }

        @GetMapping("/likes")
        public Flux<Object> getMyLikes() {
            return ReactiveSecurityContextHolder.getContext()
                    .map(ctx -> ctx.getAuthentication().getName())
                    .flatMapMany(userId -> myPageService.getAllLikes(userId));
        }

        @GetMapping("/community-posts")
        public Flux<CommunityResponseDto> getMyCommunityPosts() {
            return ReactiveSecurityContextHolder.getContext()
                    .map(ctx -> ctx.getAuthentication().getName())
                    .flatMapMany(userId -> myPageService.getMyCommunityPosts(userId));
        }



}

