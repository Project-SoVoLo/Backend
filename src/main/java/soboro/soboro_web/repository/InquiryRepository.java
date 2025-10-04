package soboro.soboro_web.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import soboro.soboro_web.domain.Inquiry;

public interface InquiryRepository extends ReactiveMongoRepository<Inquiry, String> {
}
