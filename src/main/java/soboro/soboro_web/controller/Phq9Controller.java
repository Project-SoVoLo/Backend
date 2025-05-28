package soboro.soboro_web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import soboro.soboro_web.service.FlaskClient;

import java.util.Map;

/**********************************************************
* 사용자의 입력 텍스트를 input으로 하여 모델을 활용해 phq-9 점수를 계산
* output : 원본텍스트 + phq-9 점수
* *********************************************************/

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/phq9")
public class Phq9Controller {

    private final FlaskClient flaskClient;

    @PostMapping("/predict")
    public Mono<Map> predictPhq(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return flaskClient.requestPhqPrediction(text)
                .map(flaskClient::wrapWithScore);
    }
}
