package dev.luisghtz.myaichat.chat.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VertexGeminiService {
  private final ChatClient vertextAIChatClient;
}
