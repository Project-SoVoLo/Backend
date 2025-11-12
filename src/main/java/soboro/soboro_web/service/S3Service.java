package soboro.soboro_web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key:}") // 로컬에서는 .env로 주입
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    // s3 버킷에 파일 업로드
    public Mono<String> uploadFile(FilePart filePart, String folder) {
        S3AsyncClient s3Client;

        //  로컬에서는 access/secret 키 사용, EC2는 IAM Role 자동 사용
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            s3Client = S3AsyncClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)
                            )
                    )
                    .build();
        } else {
            s3Client = S3AsyncClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }

        String key = folder + "/" + UUID.randomUUID() + "_" + filePart.filename();
        Path tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID() + "_" + filePart.filename());

        return filePart.transferTo(tempFilePath)
                .then(Mono.defer(() ->
                        Mono.fromFuture(
                                s3Client.putObject(
                                        PutObjectRequest.builder()
                                                .bucket(bucketName)
                                                .key(key)
                                                .contentType("image/jpeg")
                                                .build(),
                                        AsyncRequestBody.fromFile(tempFilePath)
                                )
                        )
                ))
                .then(Mono.fromCallable(() -> {
                    File f = tempFilePath.toFile();
                    if (f.exists()) f.delete();
                    return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
                }));
    }

    // S3 버킷의 파일 삭제
    public Mono<Void> deleteFile(String fileUrl){
        if(fileUrl == null || fileUrl.isBlank()){
            return Mono.empty();
        }

        // S3 key 추출
        String key = fileUrl.replace("https://" + bucketName + ".s3." + region + ".amazonaws.com/", "");

        S3AsyncClient s3Client;
        if(!accessKey.isBlank() && !secretKey.isBlank()){
            s3Client = S3AsyncClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)
                            )
                    )
                    .build();
        }else{
            s3Client = S3AsyncClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }

        // 삭제 요청
        return Mono.fromFuture(
                s3Client.deleteObject(
                        DeleteObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build()
                )
        ).then();
    }
}
