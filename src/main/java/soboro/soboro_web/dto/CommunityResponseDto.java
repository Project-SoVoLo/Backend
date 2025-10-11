package soboro.soboro_web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter; import lombok.Setter;
import soboro.soboro_web.domain.PostBlock;
import java.time.Instant;
import java.util.List;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityResponseDto {
    private String id;
    private String title;
    private String nickname;
    private List<PostBlock> blocks;

    private long likeCount;
    private long bookmarkCount;
    private long commentCount;

    private boolean likedByMe;
    private boolean bookmarkedByMe;

    private Instant createdAt;
    private Instant updatedAt;
}
