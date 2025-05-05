package dev.luisghtz.myaichat.prompts.models;

import java.util.List;

public class AppPrompsRequestsRoles {
  public static final String USER = "User";
  public static final String ASSISTANT = "Assistant";

  public static List<String> allStringValues() {
    return List.of(USER, ASSISTANT);
  }
}
