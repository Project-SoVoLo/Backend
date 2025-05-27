package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.CommunityPost;

public interface CommunityPostRepository extends ReactiveMongoRepository<CommunityPost, String> {

}
//save,findById,findAll,deleteById,Count 기능은 기본 제공 이외는 추가하기