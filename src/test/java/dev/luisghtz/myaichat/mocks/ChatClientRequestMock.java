package dev.luisghtz.myaichat.mocks;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.ai.chat.client.ChatClient.AdvisorSpec;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.client.ChatClient.PromptSystemSpec;
import org.springframework.ai.chat.client.ChatClient.PromptUserSpec;
import org.springframework.ai.chat.client.ChatClient.StreamResponseSpec;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChatClientRequestMock implements ChatClientRequestSpec {
  private CallResponseSpec callResponseSpec;
  private StreamResponseSpec streamResponseSpec;
  
  public ChatClientRequestMock(CallResponseSpec callResponseSpec) {
    this.callResponseSpec = callResponseSpec;
    this.streamResponseSpec = null;
  }

  @Override
  public Builder mutate() {
    return null;
  }

  @Override
  public ChatClientRequestSpec advisors(Consumer<AdvisorSpec> consumer) {
    return null;
  }

  @Override
  public ChatClientRequestSpec advisors(Advisor... advisors) {
    return null;
  }

  @Override
  public ChatClientRequestSpec advisors(List<Advisor> advisors) {
    return null;
  }

  @Override
  public ChatClientRequestSpec messages(Message... messages) {
    return this;
  }

  @Override
  public ChatClientRequestSpec messages(List<Message> messages) {
    return this;
  }

  @Override
  public <T extends ChatOptions> ChatClientRequestSpec options(T options) {
    return this;
  }

  @Override
  public ChatClientRequestSpec tools(Object... toolObjects) {
    return null;
  }

  @Override
  public ChatClientRequestSpec toolContext(Map<String, Object> toolContext) {
    return null;
  }

  @Override
  public ChatClientRequestSpec system(String text) {
    return null;
  }

  @Override
  public ChatClientRequestSpec system(Resource textResource, Charset charset) {
    return null;
  }

  @Override
  public ChatClientRequestSpec system(Resource text) {
    return null;
  }

  @Override
  public ChatClientRequestSpec system(Consumer<PromptSystemSpec> consumer) {
    return null;
  }

  @Override
  public ChatClientRequestSpec user(String text) {
    return null;
  }

  @Override
  public ChatClientRequestSpec user(Resource text, Charset charset) {
    return null;
  }

  @Override
  public ChatClientRequestSpec user(Resource text) {
    return null;
  }

  @Override
  public ChatClientRequestSpec user(Consumer<PromptUserSpec> consumer) {
    return null;
  }

  @Override
  public CallResponseSpec call() {
    return callResponseSpec;
  }

  @Override
  public StreamResponseSpec stream() {
    return streamResponseSpec;
  }

  @Override
  public ChatClientRequestSpec toolNames(String... toolNames) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toolNames'");
  }

  @Override
  public ChatClientRequestSpec toolCallbacks(ToolCallback... toolCallbacks) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toolCallbacks'");
  }

  @Override
  public ChatClientRequestSpec toolCallbacks(List<ToolCallback> toolCallbacks) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toolCallbacks'");
  }

  @Override
  public ChatClientRequestSpec toolCallbacks(ToolCallbackProvider... toolCallbackProviders) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toolCallbacks'");
  }

  @Override
  public ChatClientRequestSpec templateRenderer(TemplateRenderer templateRenderer) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'templateRenderer'");
  }

}
