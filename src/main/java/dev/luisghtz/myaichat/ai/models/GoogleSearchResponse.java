
package dev.luisghtz.myaichat.ai.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class GoogleSearchResponse {
  private String kind;
  private Url url;
  private Queries queries;
  private Context context;
  private SearchInformation searchInformation;
  private List<Item> items;


  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Url {
    private String type;
    private String template;
  }


  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Queries {
    private java.util.List<QueryRequest> request;
    private java.util.List<QueryRequest> nextPage;
  }


  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class QueryRequest {
    private String title;
    private String totalResults;
    private String searchTerms;
    private int count;
    private int startIndex;
    private String inputEncoding;
    private String outputEncoding;
    private String safe;
    private String cx;
  }


  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Context {
    private String title;
  }


  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SearchInformation {
    private double searchTime;
    private String formattedSearchTime;
    private String totalResults;
    private String formattedTotalResults;
  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Item {
    private String kind;
    private String title;
    private String htmlTitle;
    private String link;
    private String displayLink;
    private String snippet;
    private String htmlSnippet;
    private String formattedUrl;
    private String htmlFormattedUrl;
    private Map<String, Object> pagemap;
  }
}
