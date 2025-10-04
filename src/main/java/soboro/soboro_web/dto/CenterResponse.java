package soboro.soboro_web.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class CenterResponse {
    private String name;        // 병원 이름
    private String address;     // 주소
    private double distance;    // 사용자와의 거리 (km 단위)
    private String phone;       // 연락처
    private String category;    // 병원 / 상담센터 / 치료센터 (카테고리)
}
