package gov.niem.tools.api.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
