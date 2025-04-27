package gov.niem.tools.api.core.config;

import gov.niem.tools.api.core.security.SpringSecurityAuditorAware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Application configuration settings.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditor")
@EnableTransactionManagement
public class Config {

  // Constant required for use in annotations
  public static final String BASE_URL = "https://tools.niem.gov/api/v2";

  public static String baseUrl;

  public static String draft = "PENDING_BUILD";

  public static String cmfVersion;

  public static String cmfUri;

  public static String cmftoolVersion;

  /**
   * Sets configuration fields from the application properties file.
   */
  public Config(
      @Value("${app.baseUrl}") String baseUrl,
      @Value("${app.draft}") String draft,
      @Value("${app.cmf.version}") String cmfVersion,
      @Value("${app.cmf.uri}") String cmfUri,
      @Value("${app.cmftool.version}") String cmftoolVersion
  ) {

    Config.baseUrl = baseUrl;
    Config.draft = draft;
    Config.cmfVersion = cmfVersion;
    Config.cmfUri = cmfUri;
    Config.cmftoolVersion = cmftoolVersion;

  }

  /**
   * Standard supported media types for application responses (JSON, XML).
   */
  public enum AppMediaType {
    json,
    xml
  }

  /**
   * CORS settings and content negotiation configuration.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override

      public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**");
      }

      @Override
      public void configureContentNegotiation(
          @NonNull final ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("mediaType")
            .ignoreAcceptHeader(true)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("text", MediaType.TEXT_PLAIN)
            .strategies(List.of(
              new EnsureApplicationJsonNegotiationStrategy()
            ));;
      }
    };
  }

  /**
   * Enables automatic tracking of users that make database changes.
   */
  @Bean
  public AuditorAware<String> auditor() {
    return new SpringSecurityAuditorAware();
  }

  /**
   * Logs incoming HTTP requests.
   */
  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }

  /**
   * Maps database entities to JSON.
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Hibernate6Module());
    return mapper;
  }

  /**
   * Handles the conversion of HTTP request and response bodies to and from JSON.
   */
  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    return new MappingJackson2HttpMessageConverter(new Jackson2ObjectMapperBuilder()
        // .modulesToInstall(new JaxbAnnotationModule())
        .build());
  }

  /**
   * Handles the conversion of HTTP request and response bodies to and from XML.
   */
  @Bean
  public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter() {
    return new MappingJackson2XmlHttpMessageConverter(new Jackson2ObjectMapperBuilder()
        .indentOutput(true)
        .defaultUseWrapper(false)
        // .serializationInclusion(JsonInclude.Include.NON_EMPTY)
        // .modulesToInstall(new JaxbAnnotationModule())
        .createXmlMapper(true)
        .build());
  }

  /**
   * Maps XML to and from objects.
   */
  @Bean(name = "customXmlMapper")
  public XmlMapper customXmlMapper() {
    return new Jackson2ObjectMapperBuilder()
        .indentOutput(true)
        .createXmlMapper(true)
        // .propertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
        .build();
  }

}
