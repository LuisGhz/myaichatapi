package dev.luisghtz.myaichat.audio.services;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.model.Model;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.luisghtz.myaichat.audio.dto.TranscriptionResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class TranscribeService {

  private final Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> audioTranscriptionModel;

  // Default configuration constants
  private static final String DEFAULT_MODEL = "gpt-4o-mini-transcribe";
  private static final Float DEFAULT_TEMPERATURE = 0.0f;
  private static final OpenAiAudioApi.TranscriptResponseFormat DEFAULT_RESPONSE_FORMAT = OpenAiAudioApi.TranscriptResponseFormat.TEXT;

  public TranscriptionResDto transcribe(MultipartFile audio) {
    try {
      // Convert MultipartFile to Resource for Spring AI
      ByteArrayResource audioResource = new ByteArrayResource(audio.getBytes()) {
        @Override
        public String getFilename() {
          return audio.getOriginalFilename();
        }
      };

      AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioResource,
          createDefaultOptions());

      // Call OpenAI transcription service
      AudioTranscriptionResponse response = audioTranscriptionModel.call(transcriptionRequest);

      // Extract transcribed text
      String transcribedText = response.getResult().getOutput();
      log.info("Metadata {}",
          response.getResult().getMetadata().toString());
      // Return result
      return new TranscriptionResDto(transcribedText);

    } catch (Exception e) {
      throw new RuntimeException("Error transcribing audio: " + e.getMessage(), e);
    }
  }

  /**
   * Create default transcription options
   */
  private OpenAiAudioTranscriptionOptions createDefaultOptions() {
    return OpenAiAudioTranscriptionOptions.builder()
        .model(DEFAULT_MODEL)
        .responseFormat(DEFAULT_RESPONSE_FORMAT)
        .temperature(DEFAULT_TEMPERATURE)
        .build();
  }

}
