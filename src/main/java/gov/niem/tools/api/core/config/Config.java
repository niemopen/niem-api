package gov.niem.tools.api.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

import gov.niem.tools.api.core.security.SpringSecurityAuditorAware;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditor")
@EnableTransactionManagement
public class Config {

  // Constant required for use in annotations
  public final static String BASE_URL = "https://tools.niem.gov/api/v2";

  public static String baseUrl;

  public static String draft;

  public Config(@Value("${app.baseUrl}") String baseUrl, @Value("${app.draft}") String draft) {
    Config.baseUrl = baseUrl;
    Config.draft = draft;
  }

  public enum AppMediaType {
    json,
    xml
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
      }

      @Override
      public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("mediaType")
            .ignoreAcceptHeader(true)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("json", MediaType.APPLICATION_JSON);      }
    };
  }

  @Bean
  public AuditorAware<String> auditor() {
    return new SpringSecurityAuditorAware();
  }

  @Bean
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Hibernate6Module());
    return mapper;
  }

  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    return new MappingJackson2HttpMessageConverter(new Jackson2ObjectMapperBuilder()
        // .modulesToInstall(new JaxbAnnotationModule())
        .build());
  }

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

  @Bean(name = "customXmlMapper")
  public XmlMapper customXmlMapper() {
    return new Jackson2ObjectMapperBuilder()
        .indentOutput(true)
        .createXmlMapper(true)
        // .propertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE)
        .build();
  }

}
