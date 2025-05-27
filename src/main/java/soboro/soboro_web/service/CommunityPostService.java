package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.CommunityPost;
import soboro.soboro_web.repository.CommunityPostRepository;

@Service
@RequiredArgsConstructor
public class CommunityPostService {
    private final CommunityPostRepository repository;

    public Mono<CommunityPost> create(CommunityPost post) {
        return repository.save(post);
    }

    public Flux<CommunityPost> getAll() {
        return repository.findAll();
    }

    public Mono<CommunityPost> getById(String id) {
        return repository.findById(id);
    }

    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    public Mono<CommunityPost> update(String id, CommunityPost updatedPost) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setTitle(updatedPost.getTitle());
                    existing.setBlocks(updatedPost.getBlocks());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());
                    return repository.save(existing);
                });
    }
}
