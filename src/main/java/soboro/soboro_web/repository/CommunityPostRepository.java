package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.CommunityPost;

public interface CommunityPostRepository extends ReactiveMongoRepository<CommunityPost, String> {
    Flux<CommunityPost> findAllByOrderByCreatedAtDesc();
}