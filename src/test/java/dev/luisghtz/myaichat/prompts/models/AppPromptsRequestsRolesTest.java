package dev.luisghtz.myaichat.prompts.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AppPromptsRequestsRolesTest {

  @Test
  void userConstantShouldBeUser() {
    assertEquals("User", AppPromptsRequestsRoles.USER);
  }

  @Test
  void assistantConstantShouldBeAssistant() {
    assertEquals("Assistant", AppPromptsRequestsRoles.ASSISTANT);
  }

  @Test
  void allStringValuesShouldContainUserAndAssistant() {
    List<String> roles = AppPromptsRequestsRoles.allStringValues();
    assertNotNull(roles);
    assertEquals(2, roles.size());
    assertTrue(roles.contains("User"));
    assertTrue(roles.contains("Assistant"));
  }

  @Test
  void allStringValuesShouldBeImmutable() {
    List<String> roles = AppPromptsRequestsRoles.allStringValues();
    assertThrows(UnsupportedOperationException.class, () -> roles.add("Other"));
  }

  @Test
  void allStringValuesOrderShouldBeUserThenAssistant() {
    List<String> roles = AppPromptsRequestsRoles.allStringValues();
    assertEquals("User", roles.get(0));
    assertEquals("Assistant", roles.get(1));
  }
}
