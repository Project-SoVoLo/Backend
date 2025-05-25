package soboro.soboro_web.webclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// 외부 API와 통신(챗봇 구현한 API 등) 사용
@Slf4j
@Component
public class ExternalApiClient {

    private final WebClient webClient;

    public ExternalApiClient(WebClient.Builder clientBuilder){
        this.webClient = clientBuilder.baseUrl("")
                .build();
    }


}
