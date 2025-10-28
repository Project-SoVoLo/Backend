package soboro.soboro_web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLinkResponse {
    private String noticeUrl;
    private String cardnewsUrl;
    private String communityUrl;
    private String inquriyUrl;
}
