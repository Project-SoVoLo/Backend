package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import soboro.soboro_web.domain.Notice;

public interface NoticeRepository extends ReactiveMongoRepository<Notice, String> {
    // findAll(), findById(), save(), deleteById() 등 Reactive 타입으로 자동 제공
}
