package dev.luisghtz.myaichat.image;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.luisghtz.myaichat.image.providers.AwsS3Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {
  private final AwsS3Service awsS3Service;
  @Value("${cdn}")
  private String cdnUrl;

  public String uploadImage(MultipartFile file) {
    // Convert MultipartFile to byte array
    var fileName = generateImageName(file);
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
   * Generates a unique image name using UUID and the original file name.
   *
   * @param file the MultipartFile to generate the name for
   * @return the generated image name
   */
  private String generateImageName(MultipartFile file) {
    var randomUUID = UUID.randomUUID().toString();
    return "myaichat/" + randomUUID + "_" + file.getOriginalFilename();
  }
}
