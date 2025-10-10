package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Bookmark;

public interface BookmarkRepository extends ReactiveMongoRepository<Bookmark, String> {
    // 북마크 게시물 조회
    Mono<Bookmark> findByUserIdAndPostId(String userId, String postId);

    // 게시물 북마크 삭제
    Mono<Void> deleteByUserIdAndPostId(String userId, String postId);

    // 사용자 전체 북마크 조회
    Flux<Bookmark> findAllByUserId(String userId);
}
