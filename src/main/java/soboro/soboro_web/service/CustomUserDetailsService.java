package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.repository.UserRepository;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String email){
        return userRepository.findByUserEmail(email)
            .map(user -> User.builder()
                .username(user.getUserEmail())
                .password(user.getPassword())
                .roles("USER")
                .build()
            );
    }

}
