package soboro.soboro_web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter; import lombok.Setter;
import soboro.soboro_web.domain.PostBlock;
import java.util.List;

@Getter @Setter
public class CommunityRequestDto {
    @NotBlank @Size(max = 120)
    private String title;

    @Valid @Size(min = 1, max = 100)
    private List<PostBlock> blocks;
}
