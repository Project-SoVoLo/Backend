package soboro.soboro_web.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserListResponse {
    private List<UserRow> content;
    private Long total;
    private Integer page;
    private Integer size;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRow {
        private String userId;
        private String userName;
        private String userEmail;
    }
}
