package gov.niem.tools.api.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Simple web layer test.
 */
@AutoConfigureMockMvc
@SpringBootTest(classes = {AppController.class})
public class AppControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void testVersionResponse() throws Exception {
    mockMvc
        .perform(get("/version"))
        .andExpect(status().isOk());
  }

}
