package soboro.soboro_web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ReactiveSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 회원가입, 로그인 시 비밀번호 암호화
    @Bean
    public PasswordEncoder PasswordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    // 인증 필요한 요청 설정, JWT 필터 연결
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // ✅ 변경된 CORS 설정
                .authorizeExchange(ex -> ex
                        .pathMatchers(

                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",

                                "/", "/index.html",
                                "/static/**", "/css/**", "/js/**",
                                "/api/oauth/**",
                                "/favicon.ico", "/manifest.json", "/logo192.png",

                                "/api/users/login", "/api/users/register",
                                "/api/admins/register", "/api/admins/login",
                                "/api/users/update-info",
                                "/api/nlp/emotion_class","/api/diagnosis/types","/api/phq9/predict",
                                "/api/chatbot/ask",
                                "/api/rasa/classification","/api/chatbot/full",
                                "/api/inquiry/all", "/api/inquiry",
                                "/api/center",

                                "/api/notice", "/api/notice/**"

                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(httpBasic -> httpBasic.disable())
                .build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000"); // 프론트 주소
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}