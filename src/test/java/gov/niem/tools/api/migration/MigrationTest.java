package gov.niem.tools.api.migration;

import gov.niem.tools.api.Application;
import gov.niem.tools.api.TestUtils;
import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.migrate.MigrationService;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Test CMF migrations to more recent versions.
 */
@SpringBootTest(classes = {MigrationService.class, Model.class, Application.class, Config.class})
public class MigrationTest {

  @Autowired
  MigrationService migrationService;

  private static final String PATH_CMF = "migration/niem-3.0-subset.cmf.xml";

  /**
   * Test a single migration pass.
   *
   * @todo Test currently checks to see that the call succeeds. Add more detailed response checking.
   */
  @Test
  @Transactional
  public void checkMigrationSingleIteration() throws Exception {
    MultipartFile multipartFile = TestUtils.getMultipartFile(PATH_CMF);
    @SuppressWarnings("unused")
    byte[] bytes = migrationService.migrateCmf("niem", "model", "3.0", "3.1", multipartFile);
    assertTrue(true);
  }

  /**
   * Test multiple migration passes.
   *
   * @todo Test currently checks to see that the call succeeds. Add more detailed response checking.
   */
  @Test
  @Transactional
  public void checkMigrationMultiIteration() throws Exception {
    MultipartFile multipartFile = TestUtils.getMultipartFile(PATH_CMF);
    @SuppressWarnings("unused")
    byte[] bytes = migrationService.migrateCmf("niem", "model", "3.0", "3.2", multipartFile);
    assertTrue(true);
  }

}
