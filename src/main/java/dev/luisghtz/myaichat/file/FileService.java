package dev.luisghtz.myaichat.file;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.luisghtz.myaichat.file.providers.AwsS3Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
  private final AwsS3Service awsS3Service;
  @Value("${cdn}")
  private String cdnUrl;

  public String uploadFile(MultipartFile file) {
    // Convert MultipartFile to byte array
    var fileName = generateFileName(file);
    byte[] fileContent = convertToByteArray(file);

    awsS3Service.uploadFile(fileName, fileContent);
    return cdnUrl + fileName;
  }

  private byte[] convertToByteArray(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert file to byte array", e);
    }
  }

  /**
   * Generates a unique file name using UUID and the original file name.
   *
   * @param file the MultipartFile to generate the name for
   * @return the generated file name
   */
  private String generateFileName(MultipartFile file) {
    var randomUUID = UUID.randomUUID().toString();
    return "myaichat/" + randomUUID + "_" + file.getOriginalFilename();
  }
}
