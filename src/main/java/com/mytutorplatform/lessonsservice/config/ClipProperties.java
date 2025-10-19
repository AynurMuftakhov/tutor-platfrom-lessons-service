package com.mytutorplatform.lessonsservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Collections.emptyList;

@Configuration
@ConfigurationProperties(prefix = "clips")
@Data
public class ClipProperties {
  private String baseDir = System.getProperty("java.io.tmpdir") + "/turn-clips";
  private int ttlSeconds = 86_400;
  private long maxBytes = 10 * 1024 * 1024L;
  private List<String> allowedContentTypes = List.of(
      "audio/webm",
      "audio/webm;codecs=opus",
      "audio/ogg",
      "audio/wav"
  );
  private Cors cors = new Cors();

  @Data
  public static class Cors {
    private List<String> allowedOrigins = emptyList();
    private List<String> allowedMethods = List.of("GET", "POST", "DELETE", "HEAD");
  }
}
