package dev.luisghtz.myaichat.audio;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.luisghtz.myaichat.audio.dto.TranscriptionReqDto;
import dev.luisghtz.myaichat.audio.dto.TranscriptionResDto;
import dev.luisghtz.myaichat.audio.services.TranscribeService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {
  private final TranscribeService transcribeService;

  @PostMapping("transcribe")
  public ResponseEntity<TranscriptionResDto> transcribeAudio(@Validated @ModelAttribute TranscriptionReqDto dto) {
    TranscriptionResDto result = transcribeService.transcribe(dto.getAudio());
    return ResponseEntity.ok(result);
  }

}
