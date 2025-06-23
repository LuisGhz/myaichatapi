package dev.luisghtz.myaichat.mocks;

import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.core.ParameterizedTypeReference;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
public class CallResponseMock implements CallResponseSpec {

  private ChatResponse chatResponse;

  @Override
  public <T> T entity(ParameterizedTypeReference<T> type) {
    return null;
  }

  @Override
  public <T> T entity(StructuredOutputConverter<T> structuredOutputConverter) {
    return null;
  }

  @Override
  public <T> T entity(Class<T> type) {
    return null;
  }

  @Override
  public ChatResponse chatResponse() {
    return chatResponse;
  }

  @Override
  public String content() {
    return null;
  }

  @Override
  public <T> ResponseEntity<ChatResponse, T> responseEntity(Class<T> type) {
    return null;
  }

  @Override
  public <T> ResponseEntity<ChatResponse, T> responseEntity(ParameterizedTypeReference<T> type) {
    return null;
  }

  @Override
  public <T> ResponseEntity<ChatResponse, T> responseEntity(StructuredOutputConverter<T> structuredOutputConverter) {
    return null;
  }
  
}
