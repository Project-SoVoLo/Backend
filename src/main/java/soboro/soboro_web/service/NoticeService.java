
package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import soboro.soboro_web.domain.Like;
import soboro.soboro_web.domain.Notice;
import soboro.soboro_web.dto.NoticeRequestDto;
import soboro.soboro_web.dto.NoticeResponseDto;
import soboro.soboro_web.repository.LikeRepository;
import soboro.soboro_web.repository.NoticeRepository;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final LikeRepository likeRepository;
    private static final String POST_TYPE = "notice";

    // 1. 공지사항 전체 조회
    public Flux<NoticeResponseDto> getNoticeList(String userId) {
        return noticeRepository.findAll()
                .flatMap(notice ->
                        likeRepository.countByPostIdAndPostType(notice.getPostId(), POST_TYPE)
                                .flatMap(likeCount ->
                                        (userId == null || userId.isEmpty()
                                                ? Mono.just(false)
                                                : likeRepository.findByUserIdAndPostIdAndPostType(userId, notice.getPostId(), POST_TYPE)
                                                .map(like -> true)
                                                .defaultIfEmpty(false))
                                                .map(liked -> {
                                                    NoticeResponseDto dto = new NoticeResponseDto();
                                                    dto.setPostId(notice.getPostId());
                                                    dto.setTitle(notice.getTitle());
                                                    dto.setContent(notice.getContent());
                                                    dto.setDate(notice.getDate());
                                                    dto.setAdminId(notice.getAdminId());
                                                    dto.setLikeCount(likeCount.intValue());
                                                    dto.setLiked(liked);
                                                    return dto;
                                                })
                                )
                )
                .switchIfEmpty(Mono.error(new RuntimeException("등록된 공지사항이 없습니다.")));
    }

    // 2. 공지사항 작성(관리자만)
    public Mono<NoticeResponseDto> createNotice(NoticeRequestDto dto, String adminId) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setAdminId(adminId);
        notice.setPostType(POST_TYPE);

        return noticeRepository.save(notice)
                .flatMap(this::toDto)
                .onErrorResume(e ->
                        Mono.error(new RuntimeException("공지사항 저장 중 오류가 발생했습니다.")));
    }

    // 3. 공지사항 상세 조회
    public Mono<NoticeResponseDto>  getNotice(String noticeId, String userId){
        return noticeRepository.findById(noticeId)
                .switchIfEmpty(Mono.error(new RuntimeException("공지사항을 찾을 수 없습니다.")))
                .flatMap(notice ->
                        likeRepository.findByUserIdAndPostIdAndPostType(userId, noticeId, POST_TYPE)
                                .flatMap(existingLike ->
                                        likeRepository.countByPostIdAndPostType(noticeId, POST_TYPE)
                                                .map(count -> {
                                                    NoticeResponseDto dto = toDtoSync(notice, count.intValue());
                                                    dto.setLiked(true);
                                                    return dto;
                                                })
                                )
                                .switchIfEmpty(
                                        likeRepository.countByPostIdAndPostType(noticeId, POST_TYPE)
                                                .map(count -> {
                                                    NoticeResponseDto dto = toDtoSync(notice, count.intValue());
                                                    dto.setLiked(false);
                                                    return dto;
                                                })
                                )
                );
    }

    // 4. 공지사항 수정(해당글 작성한 관리자만)
    public Mono<NoticeResponseDto> updateNotice(String noticeId, NoticeRequestDto dto, String adminId) {
        return noticeRepository.findById(noticeId)
                .switchIfEmpty(Mono.error(new RuntimeException("공지사항을 찾을 수 없습니다")))
                .flatMap(notice -> {
                    if (!notice.getAdminId().equals(adminId)) {
                        return Mono.error(new RuntimeException("본인이 작성한 공지만 수정할 수 있습니다."));
                    }

                    if(dto.getTitle() != null && !dto.getTitle().isBlank()){
                        notice.setTitle(dto.getTitle());
                    }

                    if(dto.getContent() != null && !dto.getContent().isBlank()){
                        notice.setContent(dto.getContent());
                    }

                    notice.setPostType(POST_TYPE);
                    return noticeRepository.save(notice);
                })
                .flatMap(this::toDto)
                .onErrorResume(e -> Mono.error(new RuntimeException("공지사항 수정 중 오류가 발생했습니다.")));
    }

    // 5. 공지사항 삭제(해당글 작성한 관리자만)
    public Mono<Void> deleteNotice(String noticeId, String adminId){
        return noticeRepository.findById(noticeId)
                .switchIfEmpty(Mono.error(new RuntimeException("삭제할 공지사항을 찾을 수 없습니다.")))
                .flatMap(notice -> {
                    if (!notice.getAdminId().equals(adminId)) {
                        return Mono.error(new RuntimeException("본인이 작성한 공지만 삭제할 수 있습니다."));
                    }
                    return likeRepository.deleteByPostIdAndPostType(notice.getPostId(), POST_TYPE)
                            .then(noticeRepository.deleteById(noticeId));
                });
    }

    // 6. 공지사항 좋아요 토글
    public Mono<NoticeResponseDto> toggleLike(String noticeId, String userId) {
        return noticeRepository.findById(noticeId)
                .switchIfEmpty(Mono.error(new RuntimeException("공지사항을 찾을 수 없습니다.")))
                .flatMap(notice ->
                        likeRepository.findByUserIdAndPostIdAndPostType(userId, noticeId, POST_TYPE)
                                .flatMap(existing ->
                                        likeRepository.delete(existing)
                                                .thenReturn(false)
                                )
                                .switchIfEmpty(
                                        Mono.defer(() -> {
                                            Like like = new Like(userId, noticeId); // 현재 존재하는 (userId, postId) 생성자 사용
                                            like.setPostType(POST_TYPE);            // ← 여기서 타입 세팅
                                            return likeRepository.save(like).thenReturn(true);
                                        })
                                )
                                .flatMap(liked ->
                                        likeRepository.countByPostIdAndPostType(noticeId, POST_TYPE)
                                                .map(count -> {
                                                    NoticeResponseDto dto = toDtoSync(notice, count.intValue());
                                                    dto.setLiked(liked);
                                                    return dto;
                                                })
                                )
                )
                .onErrorResume(e -> Mono.error(new RuntimeException("좋아요 처리 중 오류가 발생했습니다.")));
    }

    // Entity -> Dto 변환 (비동기)
    private Mono<NoticeResponseDto> toDto(Notice notice){
        return likeRepository.countByPostIdAndPostType(notice.getPostId(), POST_TYPE)
                .map(count -> toDtoSync(notice, count.intValue()));
    }

    // 동기 변환 헬퍼
    private NoticeResponseDto toDtoSync(Notice notice, int likeCount) {
        NoticeResponseDto dto = new NoticeResponseDto();
        dto.setPostId(notice.getPostId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setDate(notice.getDate());
        dto.setAdminId(notice.getAdminId());
        dto.setLikeCount(likeCount);
        return dto;
    }
}
