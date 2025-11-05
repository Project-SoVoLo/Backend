package soboro.soboro_web.service;

import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Sentiment;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

/*************************************************************************
 * 사용자 입력 텍스트 -> Google NLP 감정 분석 -> 점수를 긍정/중립/부정 클래스 분류하여 반환
 * ************************************************************************/

@Service
public class GoogleNlpService {
    private final LanguageServiceClient language;

    // google cloud api key 불러오기 (키 json 파일 없을 시 기능 비활성화 됨 - 팀원 간 공유)
    public GoogleNlpService(@Value("${google.keyfile}") String keyfilePath, ResourceLoader resourceLoader) {
        try {
            Resource resource = resolveResource(resourceLoader, keyfilePath);
            LanguageServiceClient client = null;

            if (resource == null || !resource.exists()) {
                System.out.println("⚠️ google-nlp-key.json not found. NLP 기능은 비활성화됩니다.");
            } else {
                try (InputStream stream = resource.getInputStream()) {
                    LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                            .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(stream))
                            .build();

                    client = LanguageServiceClient.create(settings);
                }
            }

            this.language = client;
        } catch (Exception e) {
            throw new RuntimeException("Google NLP 초기화 실패", e);
        }
    }

    private Resource resolveResource(ResourceLoader resourceLoader, String keyfilePath) {
        String path = keyfilePath == null ? "" : keyfilePath.trim();
        if (path.isBlank()) {
            return resourceLoader.getResource("classpath:sovolo_nlp_key_jy.json");
        }

        Resource resource = resourceLoader.getResource(path);
        if (resource.exists()) {
            return resource;
        }

        if (!path.startsWith("file:")) {
            Resource fileResource = resourceLoader.getResource("file:" + path);
            if (fileResource.exists()) {
                return fileResource;
            }
        }

        if (!path.startsWith("classpath:")) {
            Resource classpathResource = resourceLoader.getResource("classpath:" + path);
            if (classpathResource.exists()) {
                return classpathResource;
            }
        }

        return resourceLoader.getResource("classpath:sovolo_nlp_key_jy.json");
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