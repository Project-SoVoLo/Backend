package soboro.soboro_web.webclient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import soboro.soboro_web.dto.CenterResponse;

import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class KakaoLocalClient {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .build();


    @Value("${kakao.client-id}")
    private String kakaoKey;

    // 지도에서 검색 시 관련 키워드
    private static final List<String> KEYWORDS = List.of(
            "정신과", "정신건강", "정신건강의학과", "상담센터", "심리상담", "치료센터", "중독관리통합지원센터", "정신건강복지센터"
    );


    // 기본 탐색
    public Flux<CenterResponse> search(double lat, double lng, int radiusM, int maxResults) {
        // 카테고리 탐색(HP8) + 키워드 탐색을 합치고, place_url 또는 id 기준 중복 제거
        Flux<CenterResponse> byCategory = categorySearch(lat, lng, radiusM, "HP8");
        Flux<CenterResponse> byKeywords = Flux.fromIterable(KEYWORDS)
                .flatMap(kw -> keywordSearch(lat, lng, radiusM, kw));

        return Flux.merge(byCategory, byKeywords)
                .distinct(r -> r.getName() + "|" + r.getAddress()) // 간단 중복 키
                .take(maxResults);
    }


    // 카테고리 선택 검색
    private Flux<CenterResponse> categorySearch(double lat, double lng, int radiusM, String categoryCode) {
        MultiValueMap<String, String> params = baseParams(lat, lng, radiusM);
        params.add("category_group_code", categoryCode);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v2/local/search/category.json")
                        .queryParams(params).build())
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoKey)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMapMany(this::mapToCenters);
    }


    // 키워드 검색
    private Flux<CenterResponse> keywordSearch(double lat, double lng, int radiusM, String keyword) {
        MultiValueMap<String, String> params = baseParams(lat, lng, radiusM);
        params.add("query", keyword);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v2/local/search/keyword.json")
                        .queryParams(params).build())
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoKey)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMapMany(this::mapToCenters);
    }

    private MultiValueMap<String, String> baseParams(double lat, double lng, int radiusM) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("x", String.valueOf(lng)); // Kakao는 x=lng, y=lat
        params.add("y", String.valueOf(lat));
        params.add("radius", String.valueOf(Math.min(radiusM, 20000))); // 카카오 최대 20km
        params.add("size", "15");
        return params;
    }

    @SuppressWarnings("unchecked")
    private Flux<CenterResponse> mapToCenters(Map<String, Object> kakaoResp) {
        List<Map<String, Object>> docs = (List<Map<String, Object>>) kakaoResp.getOrDefault("documents", List.of());
        return Flux.fromIterable(docs).map(doc -> {
            String name = asStr(doc.get("place_name"));
            String roadAddr = asStr(doc.get("road_address_name"));
            String addr = roadAddr.isBlank() ? asStr(doc.get("address_name")) : roadAddr;
            String phone = asStr(doc.get("phone"));
            String categoryName = asStr(doc.get("category_name"));
            String distanceMStr = asStr(doc.get("distance")); // 제공될 때만 존재(좌표검색 시)
            double distanceKm = 0.0;
            try {
                if (!distanceMStr.isBlank()) distanceKm = Math.round(Double.parseDouble(distanceMStr) / 100.0) / 10.0; // 1 decimal
            } catch (Exception ignored) {}

            return CenterResponse.builder()
                    .name(name)
                    .address(addr)
                    .distance(distanceKm)
                    .phone(phone)
                    .category(mapCategory(categoryName, name))
                    .build();
        });
    }

    private static String mapCategory(String categoryName, String placeName) {
        var text = (categoryName + " " + placeName).toLowerCase(Locale.ROOT);
        if (Stream.of("정신건강의학과", "정신과", "psychi").anyMatch(text::contains)) return "정신과";
        if (Stream.of("상담", "심리").anyMatch(text::contains)) return "상담센터";
        return "치료센터";
    }

    private static String asStr(Object o) { return o == null ? "" : String.valueOf(o); }
}
