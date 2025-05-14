package soboro.soboro_web.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
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

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {
    // 요청 헤더의 JWT 검증 & 사용자 인증 객체(Security Context) 주입
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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        String token = resolveToken(exchange.getRequest());

        if(token != null && jwtUtil.validateToken(token)){
            String email = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            return userDetailsService.findByUsername(email)
                    .flatMap(userDetails -> {
                        List<GrantedAuthority> authorityList =
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)); // 권한 부여
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        // 인증 정보를 SecurityContext에 담고 다음 필터로
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                    });
        }
        // 토큰이 없거나 유효하지 않으면 다음 필터로 넘어감
         return chain.filter(exchange);
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if( !authHeaders.isEmpty() && authHeaders.get(0).startsWith("Bearer ")){
            return authHeaders.get(0).substring(7);  // "Bearer " 다음부터 잘라냄
        }
        return null;
    }
}
