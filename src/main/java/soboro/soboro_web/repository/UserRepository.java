package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.User;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUserEmail(String email);

    Mono<?> save(org.springframework.security.core.userdetails.User user);
}
