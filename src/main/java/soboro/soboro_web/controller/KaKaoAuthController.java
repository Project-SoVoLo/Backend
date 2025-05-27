// 카카오 로그인 페이지로 리디렉션하기
package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KaKaoAuthController {

    private final WebClient webClient = WebClient.create();
    private final UserRepository userRepository;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @GetMapping("/api/oauth/kakao/login")
    public Mono<ResponseEntity<Void>> redirectToKakao() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;

        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, kakaoAuthUrl)
                .build());
    }

    @PostMapping("/api/oauth/kakao/token")
    public Mono<ResponseEntity<Map<String, Object>>> handleKakaoCallback(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=authorization_code&client_id=" + clientId +
                        "&redirect_uri=" + redirectUri +
                        "&code=" + code)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");

                    return webClient.get()
                            .uri("https://kapi.kakao.com/v2/user/me")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .flatMap(userInfo -> {
                                Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                                String email = (String) kakaoAccount.get("email");
                                String nickname = (String) profile.get("nickname");

                                return userRepository.findByUserEmail(email)
                                        .switchIfEmpty(
                                                userRepository.save(new User(email, nickname))
                                        )
                                        .flatMap(user -> {
                                            Map<String, Object> result = new HashMap<>();
                                            result.put("message", "카카오 로그인 성공");
                                            result.put("userEmail", email);
                                            result.put("nextStep", "/signup-extra-info");

                                            return Mono.just(ResponseEntity.ok(result));
                                        });
                            });
                });
    }

    @GetMapping("/api/oauth/kakao/callback")
    public Mono<ResponseEntity<Map<String, Object>>> kakaoCallback(@RequestParam("code") String code) {
        System.out.println("✅ 인가 코드 도착: " + code);

        return webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(token -> System.out.println("✅ 토큰 응답: " + token))
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");

                    return webClient.get()
                            .uri("https://kapi.kakao.com/v2/user/me")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .doOnNext(userInfo -> System.out.println("✅ 사용자 정보 응답: " + userInfo))
                            .flatMap(userInfo -> {
                                Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                                String email = (String) kakaoAccount.get("email");
                                String nickname = (String) profile.get("nickname");

                                return userRepository.findByUserEmail(email)
                                        .switchIfEmpty(userRepository.save(new User(email, nickname)))
                                        .doOnNext(user -> System.out.println("✅ 사용자 저장 완료: " + user.getUserEmail()))
                                        .flatMap(user -> {
                                            Map<String, Object> result = new HashMap<>();
                                            result.put("message", "카카오 로그인 성공");
                                            result.put("userEmail", email);
                                            result.put("nextStep", "/signup-extra-info");
                                            return Mono.just(ResponseEntity.ok(result));
                                        });
                            });
                })
                .doOnError(e -> {
                    System.out.println("❌ 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                });
    }

}
