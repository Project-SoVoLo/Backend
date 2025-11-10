package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public Mono<String> uploadFile(FilePart filePart, String folder) {
        // IAM Role 기반 인증 (Access Key 불필요)
        S3AsyncClient s3Client = S3AsyncClient.builder()
                .region(Region.of(region))
                .build();

        String key = folder + "/" + UUID.randomUUID() + "_" + filePart.filename();

        return filePart.transferTo(Paths.get("/tmp/" + filePart.filename())) // 임시 저장
                .then(Mono.fromFuture(
                        s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(key)
                                        .contentType("image/jpeg")
                                        .build(),
                                AsyncRequestBody.fromFile(Paths.get("/tmp/" + filePart.filename()))
                        )
                ))
                .thenReturn("https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key);
    }
}
