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
                .authorizeExchange(ex -> ex
                        .pathMatchers(

                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/webjars/**",

                                "/api/users/login", "/api/users/register",
                                "/api/admins/register", "/api/admins/login",
                                "/api/users/update-info","/api/logout",
                                "/api/nlp/emotion_class","/api/diagnosis/types","/api/phq9/predict",
                                "/api/chatbot/ask",
                                "/api/rasa/classification","/api/chatbot/full"

                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(httpBasic -> httpBasic.disable())
                .build();

    }

}
