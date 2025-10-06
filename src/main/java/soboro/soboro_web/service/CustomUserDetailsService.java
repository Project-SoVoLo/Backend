package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.repository.AdminRepository;
import soboro.soboro_web.repository.UserRepository;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public Mono<UserDetails> findByUsername(String email){

        // 일반 유저 조회
        Mono<UserDetails> userMono = userRepository.findByUserEmail(email)
                .map(user -> User.withUsername(user.getUserEmail())
                        // 카카오 로그인 유저는 password가 없으니까 빈 문자열으로 채워넣기
                        .password(user.getPassword() != null ? user.getPassword() : "")
                        .roles("USER")
                        .build()
                );

        // 관리자 조회
        Mono<UserDetails> adminMono = adminRepository.findByUserEmail(email)
                .map(admin -> User.withUsername(admin.getUserEmail())
                        .password(admin.getPassword() != null ? admin.getPassword() : "")
                        .roles("ADMIN")
                        .build()
                );

        return userMono.switchIfEmpty(adminMono);
    }

}