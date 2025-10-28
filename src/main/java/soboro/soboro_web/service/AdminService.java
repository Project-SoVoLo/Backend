package soboro.soboro_web.service;

import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Admin;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.dto.AdminAuthRequest;
import soboro.soboro_web.dto.AdminCommentFeedResponse;
import soboro.soboro_web.dto.AdminLinkResponse;
import soboro.soboro_web.dto.AdminResponse;
import soboro.soboro_web.dto.AdminUserListResponse;
import soboro.soboro_web.jwt.JwtUtil;
import soboro.soboro_web.repository.AdminRepository;
import soboro.soboro_web.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final int SIZE_MAX = 50;

    @Value("${admin.dashboard.base-url:https://service.sovolo.dev}")
    private String dashboardBaseUrl;

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<AdminResponse> register(AdminAuthRequest req) {
        String email = safe(req.getUserEmail());
        String rawPw = safe(req.getPassword());
        String name = safe(req.getUserName());

        if (email.isBlank() || rawPw.isBlank() || name.isBlank()) {
            return Mono.error(new IllegalArgumentException("필수 입력값 누락 (이메일/비밀번호/이름)"));
        }

        return adminRepository.existsByUserEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("이미 가입된 관리자 이메일 입니다."));
                    }
                    Admin admin = new Admin();
                    admin.setUserEmail(email);
                    admin.setPassword(passwordEncoder.encode(rawPw));
                    admin.setUserName(name);
                    return adminRepository.save(admin);
                })
                .map(saved -> buildAuthResponse(email));
    }

    public Mono<AdminResponse> login(AdminAuthRequest req) {
        String email = safe(req.getUserEmail());
        String rawPw = safe(req.getPassword());

        if (email.isBlank() || rawPw.isBlank()) {
            return Mono.error(new IllegalArgumentException("이메일/비밀번호를 입력하세요."));
        }

        return adminRepository.findByUserEmail(email)
                .filter(admin -> passwordEncoder.matches(rawPw, admin.getPassword()))
                .map(admin -> buildAuthResponse(email))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")));
    }

    public Mono<AdminUserListResponse> listUsers(String query, int page, int size) {
        int fixedSize = Math.min(Math.max(size, 1), SIZE_MAX);
        int fixedPage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(fixedPage, fixedSize);

        String norm = (query == null || query.isBlank()) ? "" : query.trim();
        String regex = ".*" + escapeRegex(norm) + ".*";

        Mono<Long> totalMono = userRepository.countByUserNameRegexIgnoreCaseOrUserEmailRegexIgnoreCase(regex, regex);
        Flux<User> rowsFlux = userRepository.findByUserNameRegexIgnoreCaseOrUserEmailRegexIgnoreCase(regex, regex, pageable);

        return Mono.zip(
                        rowsFlux.map(u -> AdminUserListResponse.UserRow.builder()
                                        .userId(u.getUserId())
                                        .userName(u.getUserName())
                                        .userEmail(u.getUserEmail())
                                        .build())
                                .collectList(),
                        totalMono
                )
                .map(tuple -> AdminUserListResponse.builder()
                        .content(tuple.getT1())
                        .total(tuple.getT2())
                        .page(fixedPage)
                        .size(fixedSize)
                        .build());
    }

    public Mono<AdminLinkResponse> links() {
        String base = dashboardBaseUrl.endsWith("/") ? dashboardBaseUrl.substring(0, dashboardBaseUrl.length() - 1) : dashboardBaseUrl;
        return Mono.just(AdminLinkResponse.builder()
                .noticeUrl(base + "/community?tab=notice")
                .cardnewsUrl(base + "/community?tab=cardNews")
                .communityUrl(base + "/community?tab=board")
                .inquriyUrl(base + "/community?tab=suggestion")
                .build());
    }

    public Mono<AdminCommentFeedResponse> recentComments(String sinceIso, int limit) {
        int fixedLimit = Math.min(Math.max(limit, 1), SIZE_MAX);
        Instant since = parseSinceOrDefault(sinceIso);

        UnwindOperation unwind = Aggregation.unwind("comments");
        MatchOperation matchAfterUnwind = Aggregation.match(Criteria.where("comments.date").gte(since));
        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "comments.date");
        LimitOperation lim = Aggregation.limit(fixedLimit);
        ProjectionOperation project = Aggregation.project()
                .andExpression("_id").as("inquiryId")
                .andExpression("title").as("title")
                .andExpression("comments._id").as("commentId")
                .andExpression("comments.userName").as("author")
                .andExpression("comments.date").as("createdAt")
                .andExpression("substr(comments.content,0,120)").as("excerpt");

        Aggregation agg = Aggregation.newAggregation(
                unwind,
                matchAfterUnwind,
                sort,
                lim,
                project
        );

        return mongoTemplate.aggregate(agg, "inquiry", CommentAgg.class)
                .map(c -> AdminCommentFeedResponse.CommentItem.builder()
                        .commentId(c.commentId)
                        .inquiryId(c.inquiryId)
                        .title(c.title)
                        .author(c.author)
                        .createdAt(c.createdAt)
                        .excerpt(c.excerpt)
                        .inquiryPostUrl(buildInquiryUrl(c.inquiryId))
                        .build())
                .collectList()
                .map(items -> AdminCommentFeedResponse.builder().items(items).build());
    }

    private String buildInquiryUrl(String inquiryId) {
        if (inquiryId == null) {
            return null;
        }
        String base = dashboardBaseUrl.endsWith("/") ? dashboardBaseUrl.substring(0, dashboardBaseUrl.length() - 1) : dashboardBaseUrl;
        return base + "/inquiry/" + inquiryId;
    }

    private AdminResponse buildAuthResponse(String userEmail) {
        String token = jwtUtil.generateToken(userEmail, ROLE_ADMIN);
        long expiresIn = jwtUtil.getExpiration();
        return AdminResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .role(ROLE_ADMIN)
                .userEmail(userEmail)
                .build();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String escapeRegex(String input) {
        return input.replaceAll("([\\\\.^$|?*+()\\[\\]{}])", "\\\\$1");
    }

    private static Instant parseSinceOrDefault(String sinceIso) {
        try {
            return (sinceIso == null || sinceIso.isBlank())
                    ? Instant.now().minusSeconds(24 * 3600)
                    : Instant.parse(sinceIso);
        } catch (Exception e) {
            return Instant.now().minusSeconds(24 * 3600);
        }
    }

    @Getter
    public static class CommentAgg {
        private String inquiryId;
        private String title;
        private String commentId;
        private String author;
        private Instant createdAt;
        private String excerpt;
    }
}
