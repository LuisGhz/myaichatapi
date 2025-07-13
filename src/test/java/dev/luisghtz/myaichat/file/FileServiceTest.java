package dev.luisghtz.myaichat.file;

import dev.luisghtz.myaichat.file.providers.AwsS3Service;
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
class FileServiceTest {

  @Mock
  private AwsS3Service awsS3Service;

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private FileService fileService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(fileService, "cdnUrl", "https://cdn.example.com/");
  }

  @Test
  void uploadFile_ShouldUploadFileAndReturnCdnUrl() throws Exception {
    byte[] fileBytes = "test-file-content".getBytes();
    String originalFileName = "test.png";

    when(multipartFile.getBytes()).thenReturn(fileBytes);
    when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);

    String result = fileService.uploadFile(multipartFile);

    assertNotNull(result);
    assertTrue(result.startsWith("https://cdn.example.com/myaichat/"));
    assertTrue(result.endsWith("_" + originalFileName));
    verify(awsS3Service, times(1)).uploadFile(anyString(), eq(fileBytes));
  }

  @Test
  void uploadFile_ShouldThrowRuntimeException_WhenFileConversionFails() throws Exception {
    when(multipartFile.getBytes()).thenThrow(new RuntimeException("IO Error"));
    when(multipartFile.getOriginalFilename()).thenReturn("fail.png");

    RuntimeException ex = assertThrows(RuntimeException.class, () -> fileService.uploadFile(multipartFile));
    assertTrue(ex.getMessage().contains("Failed to convert file to byte array"));
    verify(awsS3Service, never()).uploadFile(anyString(), any());
  }

  @Test
  void generateFileName_ShouldContainOriginalFileName() {
    when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
    // Use reflection to access private method
    String name = ReflectionTestUtils.invokeMethod(fileService, "generateFileName", multipartFile);
    assertNotNull(name);
    assertTrue(name.startsWith("myaichat/"));
    assertTrue(name.endsWith("_photo.jpg"));
  }

  @Test
  void convertToByteArray_ShouldReturnFileBytes() throws Exception {
    byte[] expected = { 1, 2, 3 };
    when(multipartFile.getBytes()).thenReturn(expected);
    byte[] actual = ReflectionTestUtils.invokeMethod(fileService, "convertToByteArray", multipartFile);
    assertArrayEquals(expected, actual);
  }
}
