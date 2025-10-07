package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Card;

@Repository
public interface CardRepository extends ReactiveMongoRepository<Card, String> {
    Mono<Card> findByPostId(String postId);
}
