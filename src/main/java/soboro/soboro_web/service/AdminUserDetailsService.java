package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.repository.AdminRepository;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements ReactiveUserDetailsService {
    private final AdminRepository adminRepository;

    @Override
    public Mono<UserDetails> findByUsername(String email){
        return adminRepository.findByUserEmail(email)
            .map(admin -> User.builder()
                .username(admin.getUserEmail())
                .password(admin.getPassword())
                .roles("ADMIN")
                .build()
            );
    }
}
