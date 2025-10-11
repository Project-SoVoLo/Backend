package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Comment;

public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findByPostIdOrderByCreatedAtAsc(String postId);
    Mono<Long> countByPostId(String postId);
    Mono<Void> deleteByPostId(String postId);
}
