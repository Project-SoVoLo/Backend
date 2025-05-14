package soboro.soboro_web.webclient;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

// 외부 API와 통신 시(챗봇 구현한 API 등) 사용
public class ExternalApiClient {
    private final WebClient webClient;

    public ExternalApiClient(WebClient.Builder clientBuilder){
        this.webClient = clientBuilder.baseUrl("")
                .build();
    }

}
