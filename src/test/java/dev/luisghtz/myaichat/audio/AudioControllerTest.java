package dev.luisghtz.myaichat.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.luisghtz.myaichat.audio.dto.TranscriptionResDto;
import dev.luisghtz.myaichat.audio.services.TranscribeService;
import dev.luisghtz.myaichat.configurationMock.AIModelsControllerTestConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AudioController.class)
@Import(AIModelsControllerTestConfiguration.class)
@ActiveProfiles("test")
@DisplayName("AudioController Tests")
class AudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TranscribeService transcribeService;

    private MockMultipartFile mockAudioFile;
    private TranscriptionResDto transcriptionResponse;

    @BeforeEach
    void setUp() {
        // Set up test data
        mockAudioFile = new MockMultipartFile(
                "audio",
                "test-audio.mp3",
                "audio/mpeg",
                "test audio content".getBytes()
        );

        transcriptionResponse = new TranscriptionResDto("This is a test transcription");
    }

    @Nested
    @DisplayName("POST /api/audio/transcribe")
    class PostEndpoints {

        @Test
        @DisplayName("POST /api/audio/transcribe - Should successfully transcribe audio file")
        void transcribeAudio_ShouldReturnTranscriptionResult() throws Exception {
            // Arrange
            when(transcribeService.transcribe(any())).thenReturn(transcriptionResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mockAudioFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("This is a test transcription"));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle empty transcription result")
        void transcribeAudio_ShouldHandleEmptyTranscriptionResult() throws Exception {
            // Arrange
            TranscriptionResDto emptyResponse = new TranscriptionResDto("");
            when(transcribeService.transcribe(any())).thenReturn(emptyResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mockAudioFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value(""));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle null transcription result")
        void transcribeAudio_ShouldHandleNullTranscriptionResult() throws Exception {
            // Arrange
            TranscriptionResDto nullResponse = new TranscriptionResDto(null);
            when(transcribeService.transcribe(any())).thenReturn(nullResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mockAudioFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty());

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle different audio file types")
        void transcribeAudio_ShouldHandleDifferentAudioFileTypes() throws Exception {
            // Arrange
            MockMultipartFile wavFile = new MockMultipartFile(
                    "audio",
                    "test-audio.wav",
                    "audio/wav",
                    "test wav audio content".getBytes()
            );
            when(transcribeService.transcribe(any())).thenReturn(transcriptionResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(wavFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("This is a test transcription"));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle empty audio file")
        void transcribeAudio_ShouldHandleEmptyAudioFile() throws Exception {
            // Arrange
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "audio",
                    "empty-audio.mp3",
                    "audio/mpeg",
                    new byte[0]
            );
            when(transcribeService.transcribe(any())).thenReturn(transcriptionResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(emptyFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("This is a test transcription"));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle service exception")
        void transcribeAudio_ShouldHandleServiceException() throws Exception {
            // Arrange
            when(transcribeService.transcribe(any())).thenThrow(new RuntimeException("Transcription service error"));

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mockAudioFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isInternalServerError());

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle missing audio parameter")
        void transcribeAudio_ShouldHandleMissingAudioParameter() throws Exception {
            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());

            // Verify service is not called
            verify(transcribeService, never()).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle large audio files")
        void transcribeAudio_ShouldHandleLargeAudioFiles() throws Exception {
            // Arrange
            byte[] largeContent = new byte[1024 * 1024]; // 1MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "audio",
                    "large-audio.mp3",
                    "audio/mpeg",
                    largeContent
            );
            when(transcribeService.transcribe(any())).thenReturn(transcriptionResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(largeFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("This is a test transcription"));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle special characters in transcription")
        void transcribeAudio_ShouldHandleSpecialCharactersInTranscription() throws Exception {
            // Arrange
            TranscriptionResDto specialCharResponse = new TranscriptionResDto("Hello! This is a test with special characters: àáâãäåæçèéêë & symbols $%@#");
            when(transcribeService.transcribe(any())).thenReturn(specialCharResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mockAudioFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("Hello! This is a test with special characters: àáâãäåæçèéêë & symbols $%@#"));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle multiple different audio formats")
        void transcribeAudio_ShouldHandleMultipleDifferentAudioFormats() throws Exception {
            // Test MP4 format
            MockMultipartFile mp4File = new MockMultipartFile(
                    "audio",
                    "test-audio.mp4",
                    "audio/mp4",
                    "test mp4 audio content".getBytes()
            );
            when(transcribeService.transcribe(any())).thenReturn(transcriptionResponse);

            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mp4File)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value("This is a test transcription"));

            verify(transcribeService, times(1)).transcribe(any());
        }

        @Test
        @DisplayName("POST /api/audio/transcribe - Should handle long transcription text")
        void transcribeAudio_ShouldHandleLongTranscriptionText() throws Exception {
            // Arrange
            String longTranscription = "This is a very long transcription text that might exceed normal length limits. ".repeat(50);
            TranscriptionResDto longResponse = new TranscriptionResDto(longTranscription);
            when(transcribeService.transcribe(any())).thenReturn(longResponse);

            // Act & Assert
            mockMvc.perform(multipart("/api/audio/transcribe")
                    .file(mockAudioFile)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").value(longTranscription));

            // Verify service interaction
            verify(transcribeService, times(1)).transcribe(any());
        }
    }
}