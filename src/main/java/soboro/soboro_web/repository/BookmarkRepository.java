package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;

public interface BookmarkRepository extends ReactiveMongoRepository<Bookmark, String> {
    // 북마크 게시물 조회
    Mono<Bookmark> findByUserIdAndPostIdAndPostType(String userId, String postId, String postType);
    // 게시물 북마크 삭제
    Mono<Void> deleteByUserIdAndPostIdAndPostType(String userId, String postId, String postType);
    // 사용자 전체 북마크 조회
    Flux<Bookmark> findAllByUserIdAndPostType(String userId, String postType);
    //북마크 개수 카운트
    Mono<Long> countByPostIdAndPostType(String postId, String postType);
    //연쇄 삭제
    Mono<Void> deleteByPostIdAndPostType(String postId, String postType);
    Mono<Boolean> existsByUserIdAndPostIdAndPostType(String userId, String postId, String postType);

}
