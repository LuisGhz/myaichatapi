package dev.luisghtz.myaichat.configurationMock;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Model;
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

    @Bean
    @Primary
    Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> audioTranscriptionModel() {
        return mock(Model.class);
    }
}
