package dev.luisghtz.myaichat.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import dev.luisghtz.myaichat.ai.models.GoogleSearchResponse;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Component
@Log4j2
public class GoogleSearchClient {
  @Value("${google.search.api-key}")
  private String apiKey;

  @Value("${google.search.custom-search-engine-id}")
  private String customSearchEngineId;

  private final RestTemplate restTemplate;

  public GoogleSearchClient(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  /**
   * Searches Google Custom Search for the given query and returns formatted
   * results.
   */
  @Tool(name = "google_search", description = "Searches in Google to get information about a topic.")
  public String search(@JsonPropertyDescription("The search query") String query) {
    log.info("Searching Google Custom Search for query: {}", query);
    Map<String, String> params = new HashMap<>();
    params.put("key", apiKey);
    params.put("cx", customSearchEngineId);
    params.put("query", query);
    params.put("num", "5"); // Number of results to return
    String url = "https://www.googleapis.com/customsearch/v1?key={key}&cx={cx}&q={query}&num={num}";
    GoogleSearchResponse response = restTemplate.getForObject(
        url, GoogleSearchResponse.class,
        params);
    if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
      return "No results found.";
    }
    StringBuilder sb = new StringBuilder();
    for (GoogleSearchResponse.Item item : response.getItems()) {
      sb.append(item.getTitle())
          .append(": ")
          .append(item.getSnippet())
          .append(" (")
          .append(item.getLink())
          .append(")\n");
    }
    return sb.toString();
  }
}
