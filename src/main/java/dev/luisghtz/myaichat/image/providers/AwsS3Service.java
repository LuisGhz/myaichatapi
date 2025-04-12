package dev.luisghtz.myaichat.image.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Log4j2
public class AwsS3Service {

  @Value("${aws.s3.access-key}")
  private String accessKey;
  @Value("${aws.s3.secret-key}")
  private String secretKey;
  @Value("${aws.s3.bucket-name}")
  private String bucketName;

  private S3Client s3Client;

  @PostConstruct
  public void init() {
    if (accessKey == null || secretKey == null) {
      throw new RuntimeException("AWS credentials not found in environment variables.");
    }
    // Initialize the S3 client with the region and credentials
    this.s3Client = S3Client.builder()
        .region(Region.MX_CENTRAL_1)
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

  public void uploadFile(String fileName, byte[] fileContent) {
    // Implement the logic to upload the file to S3 using the s3Client
    // For example:
    s3Client.putObject(PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build(), RequestBody.fromBytes(fileContent));
  }
}
