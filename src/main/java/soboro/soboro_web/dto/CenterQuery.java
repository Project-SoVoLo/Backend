package soboro.soboro_web.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter @Setter
public class CenterQuery {
    @NotNull
    private Double lat; // 위도
    @NotNull
    private Double lng; // 경도

    private Integer radiusM;    // 반경(m 단위)
}
