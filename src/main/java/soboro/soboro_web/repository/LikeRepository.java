package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Like;

@Repository
public interface LikeRepository extends ReactiveMongoRepository<Like, String> {
    //좋아요 눌렀는지
    Mono<Like> findByUserIdAndPostId(String userId, String postId);

    // 특정 게시글 좋아요 개수
    Mono<Long> countByPostId(String postId);

    // 특정 게시글 좋아요 모두 삭제 (게시글 삭제 시 필요)
    Mono<Void> deleteByPostId(String postId);
    Mono<Void> deleteByUserIdAndPostId(String userId, String postId);

    // 사용자 전체 좋아요 글 조회
    Flux<Like> findAllByUserId(String userId);
}