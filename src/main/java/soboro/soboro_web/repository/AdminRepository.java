package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Admin;

public interface AdminRepository extends ReactiveMongoRepository<Admin, String> {
    Mono<Admin> findByUserEmail(String email);
     // 이메일 중복 체크
    Mono<Boolean> existsByUserEmail(String email);
}
