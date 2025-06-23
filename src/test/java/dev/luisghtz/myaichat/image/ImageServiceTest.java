package dev.luisghtz.myaichat.image;

import dev.luisghtz.myaichat.image.providers.AwsS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

  @Mock
  private AwsS3Service awsS3Service;

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private ImageService imageService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(imageService, "cdnUrl", "https://cdn.example.com/");
  }

  @Test
  void uploadImage_ShouldUploadFileAndReturnCdnUrl() throws Exception {
    byte[] fileBytes = "test-image-content".getBytes();
    String originalFileName = "test.png";

    when(multipartFile.getBytes()).thenReturn(fileBytes);
    when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);

    String result = imageService.uploadImage(multipartFile);

    assertNotNull(result);
    assertTrue(result.startsWith("https://cdn.example.com/myaichat/"));
    assertTrue(result.endsWith("_" + originalFileName));
    verify(awsS3Service, times(1)).uploadFile(anyString(), eq(fileBytes));
  }

  @Test
  void uploadImage_ShouldThrowRuntimeException_WhenFileConversionFails() throws Exception {
    when(multipartFile.getBytes()).thenThrow(new RuntimeException("IO Error"));
    when(multipartFile.getOriginalFilename()).thenReturn("fail.png");

    RuntimeException ex = assertThrows(RuntimeException.class, () -> imageService.uploadImage(multipartFile));
    assertTrue(ex.getMessage().contains("Failed to convert file to byte array"));
    verify(awsS3Service, never()).uploadFile(anyString(), any());
  }

  @Test
  void generateImageName_ShouldContainOriginalFileName() {
    when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
    // Use reflection to access private method
    String name = ReflectionTestUtils.invokeMethod(imageService, "generateImageName", multipartFile);
    assertNotNull(name);
    assertTrue(name.startsWith("myaichat/"));
    assertTrue(name.endsWith("_photo.jpg"));
  }

  @Test
  void convertToByteArray_ShouldReturnFileBytes() throws Exception {
    byte[] expected = { 1, 2, 3 };
    when(multipartFile.getBytes()).thenReturn(expected);
    byte[] actual = ReflectionTestUtils.invokeMethod(imageService, "convertToByteArray", multipartFile);
    assertArrayEquals(expected, actual);
  }
}
