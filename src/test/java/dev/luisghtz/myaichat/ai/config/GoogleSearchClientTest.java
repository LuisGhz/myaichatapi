package dev.luisghtz.myaichat.ai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import dev.luisghtz.myaichat.ai.models.GoogleSearchResponse;

@ExtendWith(MockitoExtension.class)
class GoogleSearchClientTest {

  @Nested
  @DisplayName("search - no results")
  class NoResults {
    @Test
    @DisplayName("Should return friendly message when no results returned")
    void shouldReturnNoResultsMessage() {
      RestTemplate restTemplate = mock(RestTemplate.class);
      RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
      when(builder.build()).thenReturn(restTemplate);

      GoogleSearchClient client = new GoogleSearchClient(builder);
      // mock RestTemplate to return an empty response
      when(restTemplate.getForObject(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(GoogleSearchResponse.class), org.mockito.ArgumentMatchers.anyMap()))
          .thenReturn(new GoogleSearchResponse());

      String result = client.search("anything");
      assertThat(result).contains("No results");
    }
  }

  @Nested
  @DisplayName("search - results present")
  class ResultsPresent {
    @Test
    @DisplayName("Should format returned items into a readable string")
    void shouldFormatItems() {
      RestTemplate restTemplate = mock(RestTemplate.class);
      RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
      when(builder.build()).thenReturn(restTemplate);

      GoogleSearchClient client = new GoogleSearchClient(builder);
      GoogleSearchResponse resp = new GoogleSearchResponse();
      GoogleSearchResponse.Item item = new GoogleSearchResponse.Item();
      item.setTitle("Title 1");
      item.setSnippet("Snippet 1");
      item.setLink("http://example.com/1");
      resp.setItems(List.of(item));

      when(restTemplate.getForObject(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(GoogleSearchResponse.class), org.mockito.ArgumentMatchers.anyMap()))
          .thenReturn(resp);

      String result = client.search("query");
      assertThat(result).contains("Title 1").contains("Snippet 1").contains("http://example.com/1");
    }
  }
}
