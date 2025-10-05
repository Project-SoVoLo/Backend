package soboro.soboro_web.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import soboro.soboro_web.dto.CenterResponse;
import soboro.soboro_web.webclient.KakaoLocalClient;


@Service
@RequiredArgsConstructor
public class CenterSearchService {

    private final KakaoLocalClient kakao;

    @Value("${center.search-radius-m:2000}")
    private int defaultRadiusM;

    @Value("${center.max-results:20}")
    private int maxResults;

    public Flux<CenterResponse> search(double lat, double lng, Integer radiusM) {
        int r = (radiusM == null || radiusM <= 0) ? defaultRadiusM : radiusM;
        return kakao.search(lat, lng, r, maxResults);
    }
}
