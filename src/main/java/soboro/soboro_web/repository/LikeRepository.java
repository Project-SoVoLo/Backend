package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Like;

@Repository
public interface LikeRepository extends ReactiveMongoRepository<Like, String> {
    //좋아요 눌렀는지
    Mono<Like> findByUserIdAndPostIdAndPostType(String userId, String postId, String postType);
    // 특정 게시글 좋아요 개수
    Mono<Long> countByPostIdAndPostType(String postId, String postType);
    // 특정 게시글 좋아요 모두 삭제 (게시글 삭제 시 필요)
    Mono<Void> deleteByPostIdAndPostType(String postId, String postType);
    Mono<Void> deleteByUserIdAndPostIdAndPostType(String userId, String postId, String postType);
    // 사용자 전체 좋아요 글 조회
    Flux<Like> findAllByUserIdAndPostType(String userId, String postType);

    Mono<Boolean> existsByUserIdAndPostIdAndPostType(String userId, String postId, String postType);

}