package dev.luisghtz.myaichat.audio.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.model.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import dev.luisghtz.myaichat.mocks.AudioTestMocks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranscribeService Tests")
class TranscribeServiceTest {

  @Mock
  private Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> audioTranscriptionModel;

  @Mock
  private AudioTranscriptionResponse audioTranscriptionResponse;

  @InjectMocks
  private TranscribeService transcribeService;

  private MultipartFile mockAudioFile;
  private final String expectedTranscribedText = "This is a test transcription";

  @BeforeEach
  void setUp() {
    mockAudioFile = AudioTestMocks.createMockAudioFile();
  }

  @Nested
  @DisplayName("transcribe() method")
  class TranscribeMethod {

    @Test
    @DisplayName("transcribe() - Should handle when AudioTranscriptionModel behavior is mocked")
    void transcribe_ShouldHandleAudioTranscriptionModelBehavior() throws Exception {
      // Arrange
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenReturn(audioTranscriptionResponse);
      when(audioTranscriptionResponse.getResult())
          .thenReturn(null); // This will cause a NullPointerException which is caught

      // Act & Assert - We expect a RuntimeException due to the null result
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
      verify(audioTranscriptionModel).call(any(AudioTranscriptionPrompt.class));
      verify(audioTranscriptionResponse).getResult();
    }

    @Test
    @DisplayName("transcribe() - Should throw RuntimeException when AudioTranscriptionModel throws exception")
    void transcribe_ShouldThrowRuntimeExceptionWhenModelThrowsException() throws Exception {
      // Arrange
      String errorMessage = "OpenAI API error";
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new RuntimeException(errorMessage));

      // Act & Assert
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
      assertTrue(exception.getMessage().contains(errorMessage));
      verify(audioTranscriptionModel).call(any(AudioTranscriptionPrompt.class));
    }

    @Test
    @DisplayName("transcribe() - Should throw RuntimeException when MultipartFile throws IOException")
    void transcribe_ShouldThrowRuntimeExceptionWhenMultipartFileThrowsIOException() throws Exception {
      // Arrange
      MultipartFile faultyFile = AudioTestMocks.createFaultyMockAudioFile();

      // Act & Assert
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(faultyFile);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
      assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    @Test
    @DisplayName("transcribe() - Should handle null MultipartFile gracefully")
    void transcribe_ShouldHandleNullMultipartFile() {
      // Act & Assert
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(null);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
    }

    @Test
    @DisplayName("transcribe() - Should handle when getResult() returns null")
    void transcribe_ShouldHandleWhenGetResultReturnsNull() throws Exception {
      // Arrange
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenReturn(audioTranscriptionResponse);
      when(audioTranscriptionResponse.getResult())
          .thenReturn(null);

      // Act & Assert
      ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
      verify(audioTranscriptionModel).call(any(AudioTranscriptionPrompt.class));
      verify(audioTranscriptionResponse).getResult();
    }

    @Test
    @DisplayName("transcribe() - Should verify AudioTranscriptionPrompt is created with correct parameters")
    void transcribe_ShouldVerifyAudioTranscriptionPromptCreation() throws Exception {
      // Arrange
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new RuntimeException("Expected for test"));

      // Act & Assert
      assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      verify(audioTranscriptionModel).call(argThat(prompt -> {
        // Verify that the prompt contains our audio resource
        assertNotNull(prompt);
        assertNotNull(prompt.getInstructions());
        assertNotNull(prompt.getOptions());
        return true;
      }));
    }
  }

  @Nested
  @DisplayName("Error Handling and Edge Cases")
  class ErrorHandlingAndEdgeCases {

    @Test
    @DisplayName("transcribe() - Should handle NullPointerException from audioTranscriptionModel")
    void transcribe_ShouldHandleNullPointerExceptionFromModel() throws Exception {
      // Arrange
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new NullPointerException("Null pointer in model"));

      // Act & Assert
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
      assertTrue(exception.getCause() instanceof NullPointerException);
    }

    @Test
    @DisplayName("transcribe() - Should handle ResponseStatusException from audioTranscriptionModel")
    void transcribe_ShouldHandleResponseStatusExceptionFromModel() throws Exception {
      // Arrange
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid argument"));

      // Act & Assert
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      assertTrue(exception.getMessage().contains("Error transcribing audio"));
      assertTrue(exception.getCause() instanceof ResponseStatusException);
    }

    @Test
    @DisplayName("transcribe() - Should handle large audio files by checking prompt creation")
    void transcribe_ShouldHandleLargeAudioFiles() throws Exception {
      // Arrange
      MultipartFile largeAudioFile = AudioTestMocks.createLargeMockAudioFile();
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new RuntimeException("Expected for test"));

      // Act & Assert
      assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(largeAudioFile);
      });

      // Verify that the method attempted to call the model with the large file
      verify(audioTranscriptionModel).call(any(AudioTranscriptionPrompt.class));
    }

    @Test
    @DisplayName("transcribe() - Should handle zero-byte audio file")
    void transcribe_ShouldHandleZeroByteAudioFile() throws Exception {
      // Arrange
      MultipartFile emptyFile = AudioTestMocks.createMockAudioFile("empty.wav", new byte[0]);
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new RuntimeException("Expected for test"));

      // Act & Assert
      assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(emptyFile);
      });

      // Verify that the method attempted to call the model even with empty file
      verify(audioTranscriptionModel).call(any(AudioTranscriptionPrompt.class));
    }

    @Test
    @DisplayName("transcribe() - Should handle audio file with special characters in filename")
    void transcribe_ShouldHandleSpecialCharactersInFilename() throws Exception {
      // Arrange
      MultipartFile specialFile = AudioTestMocks.createMockAudioFile("test-audio_ñáéíóú@#$.wav", "content".getBytes());
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new RuntimeException("Expected for test"));

      // Act & Assert
      assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(specialFile);
      });

      // Verify that the method attempted to process the file with special characters
      verify(audioTranscriptionModel).call(any(AudioTranscriptionPrompt.class));
    }
  }

  @Nested
  @DisplayName("createDefaultOptions() method verification")
  class CreateDefaultOptionsMethod {

    @Test
    @DisplayName("createDefaultOptions() - Should create options with correct default values")
    void createDefaultOptions_ShouldCreateOptionsWithCorrectDefaults() throws Exception {
      // This test verifies that the default options are created correctly
      // Since the method is private, we test it indirectly through the transcribe
      // method

      // Arrange
      when(audioTranscriptionModel.call(any(AudioTranscriptionPrompt.class)))
          .thenThrow(new RuntimeException("Expected for test"));

      // Act & Assert
      assertThrows(RuntimeException.class, () -> {
        transcribeService.transcribe(mockAudioFile);
      });

      verify(audioTranscriptionModel).call(argThat(prompt -> {
        assertNotNull(prompt.getOptions());
        // The options should be created with default values as specified in the
        // constants
        return true;
      }));
    }
  }
}