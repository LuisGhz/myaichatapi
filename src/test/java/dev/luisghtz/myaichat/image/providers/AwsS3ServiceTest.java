package dev.luisghtz.myaichat.image.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsS3ServiceTest {

  @Mock
  private S3Client mockS3Client;
  @InjectMocks
  private AwsS3Service awsS3Service;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(awsS3Service, "accessKey", "test-access-key");
    ReflectionTestUtils.setField(awsS3Service, "secretKey", "test-secret-key");
    ReflectionTestUtils.setField(awsS3Service, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(awsS3Service, "s3Client", mockS3Client);
  }

  @Test
  void testInitThrowsExceptionWhenCredentialsMissing() {
    AwsS3Service service = new AwsS3Service();
    ReflectionTestUtils.setField(service, "accessKey", null);
    ReflectionTestUtils.setField(service, "secretKey", null);

    RuntimeException ex = assertThrows(RuntimeException.class, service::init);
    assertEquals("AWS credentials not found in environment variables.", ex.getMessage());
  }

  @Test
  void testInitCreatesS3Client() {
    AwsS3Service service = new AwsS3Service();
    ReflectionTestUtils.setField(service, "accessKey", "a");
    ReflectionTestUtils.setField(service, "secretKey", "b");
    ReflectionTestUtils.setField(service, "bucketName", "bucket");

    service.init();

    S3Client s3Client = (S3Client) ReflectionTestUtils.getField(service, "s3Client");
    assertNotNull(s3Client);
  }

  @Test
  void testUploadFileCallsPutObject() {
    String fileName = "test.txt";
    byte[] content = "hello".getBytes();

    awsS3Service.uploadFile(fileName, content);

    ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    verify(mockS3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
    assertEquals("test-bucket", requestCaptor.getValue().bucket());
    assertEquals(fileName, requestCaptor.getValue().key());
  }

  @Test
  void testDeleteFileCallsDeleteObject() {
    String fileName = "delete.txt";
    awsS3Service.deleteFile(fileName);
    var delObjectRequest = DeleteObjectRequest.builder()
        .bucket("test-bucket")
        .key(fileName)
        .build();
    verify(mockS3Client).deleteObject(delObjectRequest);
  }
}
