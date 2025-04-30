package soboro.soboro_web.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;
import soboro.soboro_web.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> register(User user) {
        return userRepository.save(user);
    }

    public Mono<User> findByUserEmail(String email) {
        return userRepository.findByUserEmail(email);
    }
}
