package gov.niem.tools.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import gov.niem.tools.api.core.config.Config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Commonly used functionality behind NIEM supported tools
 */
@SpringBootApplication
@EnableJpaRepositories
public class Application {

  /**
   * Runs the REST API and backend
   * @param args - Command-line arguments
   */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

  /**
   * Set basic OpenAPI information for the project.
   * This setup allows the version to be injected from build.gradle.
   *
   * @return - An OpenAPI object configured with NIEM API defaults.
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(
          new Info()
              .title("NIEM API 2.0")
              .description(
                  "Community support for common NIEM tool functionality to browse and search the model, build and migrate subsets, and check conformance")
              .version(Config.draft)
              .contact(
                  new Contact()
                      .name("NIEMOpen")
                      .url("https://niemopen.org")))
      .addServersItem(
          new Server()
              .url("https://tools.niem.gov/api/v2")
              .description("Production server"))
      .addServersItem(
          new Server()
              .url("http://localhost:8080/api/v2")
              .description("Local machine"))
      .addServersItem(
          new Server()
              .url("https://yellowkiwi06.icl.gtri.org/")
              .description("Internal test server"));
  }

}
