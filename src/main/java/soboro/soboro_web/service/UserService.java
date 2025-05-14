package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public Mono<User> register(User user) {
        return userRepository.existsByUserEmail(user.getUserEmail())
            .flatMap(duplicated -> {
                 if(duplicated){
                     return Mono.error(new RuntimeException("이미 가입된 유저입니다."));
                 }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                return userRepository.save(user);
            });
    }
    public Mono<User> findByUserEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

}
