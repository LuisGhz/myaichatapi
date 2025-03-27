package dev.luisghtz.myaichat.chat.dtos;

import java.util.List;

import dev.luisghtz.myaichat.chat.models.ChatSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatsListResponseDto {
  List<ChatSummary> chats;
}
