package gov.niem.tools.api.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import gov.niem.tools.api.db.ServiceHub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppController.class)
public class AppControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  ServiceHub hub;
  // AppService appService;

  @Test
  public void testVersionResponse() throws Exception {
    // when(appService.getDraft()).thenReturn("version");

    mockMvc
    .perform(get("/version"))
    .andExpect(status().isOk())
    .andExpect(content().string("version"));
  }

}
