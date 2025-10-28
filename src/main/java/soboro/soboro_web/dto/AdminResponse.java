package soboro.soboro_web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponse {
    private String token;
    private Long expiresIn;
    private String role;
    private String userEmail;
}
