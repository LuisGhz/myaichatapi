package dev.luisghtz.myaichat.configurationMock;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class AIModelsControllerTestConfiguration {

    @Bean
    @Primary
    OpenAiChatModel openAiChatModel() {
        return mock(OpenAiChatModel.class);
    }

    @Bean
    @Primary
    VertexAiGeminiChatModel vertexAiGeminiChatModel() {
        return mock(VertexAiGeminiChatModel.class);
    }

    @Bean
    @Primary
    ChatClient openAIChatClient() {
        return mock(ChatClient.class);
    }

    @Bean
    @Primary 
    ChatClient vertextAIChatClient() {
        return mock(ChatClient.class);
    }
}
