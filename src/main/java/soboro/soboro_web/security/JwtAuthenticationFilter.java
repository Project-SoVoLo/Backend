package soboro.soboro_web.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import soboro.soboro_web.jwt.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Qualifier("customUserDetailsService") ReactiveUserDetailsService userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    // 요청이 들어올 때마다 filter로 jwt 검사
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());

        // 토큰이 없으면 (예: 로그인/회원가입) 그냥 통과
        if (token == null) {
            return chain.filter(exchange);
        }

        // 토큰 상태 확인
        String status = jwtUtil.validateTokenAndGetStatus(token);

        if (!"VALID".equals(status)) {
            // 만료 또는 잘못된 토큰이면 401 + JSON 반환
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().set("Content-Type", "application/json; charset=UTF-8");

            String message = switch (status) {
                case "EXPIRED" -> "토큰이 만료되었습니다.";
                case "INVALID_SIGNATURE" -> "유효하지 않은 서명입니다.";
                case "UNSUPPORTED" -> "지원되지 않는 토큰 형식입니다.";
                case "ILLEGAL" -> "잘못된 토큰 형식입니다.";
                default -> "인증에 실패했습니다.";
            };

            String json = String.format(
                    "{\"error\":\"%s\",\"status\":\"%s\",\"nextStep\":\"/login\"}",
                    message, status
            );

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        }

        // 토큰이 유효하면 사용자 인증 주입
        String email = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        return userDetailsService.findByUsername(email)
                .flatMap(userDetails -> {
                    List<GrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(), null, authorities
                    );

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                });
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if(!authHeaders.isEmpty() && authHeaders.get(0).startsWith("Bearer ")){
            return authHeaders.get(0).substring(7);
        }
        return null;
    }
}
