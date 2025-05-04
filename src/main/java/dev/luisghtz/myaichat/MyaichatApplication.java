package dev.luisghtz.myaichat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyaichatApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyaichatApplication.class, args);
	}

  @Bean
  ChatClient openAIChatClient(OpenAiChatModel openAiChatModel) {
    return ChatClient.create(openAiChatModel);
  }

  @Bean
  ChatClient vertextAIChatClient(VertexAiGeminiChatModel vertexAiChatModel) {
    return ChatClient.create(vertexAiChatModel);
  }

}
