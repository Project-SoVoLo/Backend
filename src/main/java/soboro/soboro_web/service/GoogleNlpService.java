package soboro.soboro_web.service;

import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Sentiment;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/*************************************************************************
* 사용자 입력 텍스트 -> Google NLP 감정 분석 -> 점수를 긍정/중립/부정 클래스 분류하여 반환
* ************************************************************************/

@Service
public class GoogleNlpService {
    private final LanguageServiceClient language;

    // google cloud api key 불러오기 (키 json 파일 없을 시 기능 비활성화 됨 - 팀원 간 공유)
    public GoogleNlpService() {
        try {
            InputStream stream = new ClassPathResource("sovolo_nlp_key_dy.json").getInputStream();
            if (stream == null) {
                System.out.println("⚠️ google-nlp-key.json not found. NLP 기능은 비활성화됩니다.");
                this.language = null;
                return;
            }

            LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                    .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(stream))
                    .build();

            this.language = LanguageServiceClient.create(settings);
        } catch (Exception e) {
            throw new RuntimeException("Google NLP 초기화 실패", e);
        }
    }

    // documentSentiment.score 점수 = 전체 문장에 대한 감정 점수 (-1.0~1.0 범위)
    // 해당 점수를 범위를 나눠서 3개의 클래스 분류
    public Map<String, Object> analyzeSentiment(String text) {
        Document doc = Document.newBuilder()
                .setContent(text)
                .setType(Document.Type.PLAIN_TEXT)
                .setLanguage("ko")
                .build();

        Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
        float score = sentiment.getScore();

        String sentimentClass;
        if (score >= 0.25) sentimentClass = "positive";
        else if (score <= -0.25) sentimentClass = "negative";
        else sentimentClass = "neutral";

        return Map.of(
                "sentiment", sentimentClass,
                "score", score
        );
    }
}
