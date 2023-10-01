package gov.niem.tools.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import gov.niem.tools.api.core.config.Config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;

@ContextConfiguration(classes = {Config.class})
@SpringBootTest(properties = "spring.main.lazy-initialization=true", classes = {Config.class})
class ApplicationTest {

  /**
   * Checks that the application context loads and that the app
   * will be able to run.
   */
	@Test
	public void checkContextLoads() {
	}

  @Disabled
  @Test
  public void checkVersion() {
    String version = Config.draft;
    assertTrue(version.equals("PENDING_BUILD") || version.startsWith("2.0"));
  }

}
