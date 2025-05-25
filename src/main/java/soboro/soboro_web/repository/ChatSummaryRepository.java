package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import soboro.soboro_web.domain.ChatSummary;

public interface ChatSummaryRepository extends ReactiveMongoRepository<ChatSummary, String> {
    Flux<ChatSummary> findByUserIdOrderByDateDesc(String userId); // 최근 상담내용 기준으로 챗봇 상담 내역 보여주기
}
