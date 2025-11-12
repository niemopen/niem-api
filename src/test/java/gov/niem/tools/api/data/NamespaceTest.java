package gov.niem.tools.api.data;

import gov.niem.tools.api.Application;
import gov.niem.tools.api.core.config.Config;
import gov.niem.tools.api.core.utils.CmfUtils;
import gov.niem.tools.api.db.base.AddModelReason;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.namespace.NamespaceService;

import org.mitre.niem.cmf.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Pull property data from a loaded database to ensure expected field values are returned.
 */
@SpringBootTest(classes = {
  Application.class, Config.class, NamespaceService.class, Namespace.class
})
public class NamespaceTest {

  @Autowired
  NamespaceService namespaceService;

  @Test
  @Transactional
  public void testNamespaceObject() {
    Namespace namespace = namespaceService.findOne("niem", "model", "6.0", "nc");
    assertEquals("NIEM Core", namespace.getName());
    assertEquals("nc", namespace.getPrefix());
  }

  @Test
  @Transactional
  public void testNamespaceCmf() throws Exception {
    Namespace namespace = namespaceService.findOne("niem", "model", "6.0", "nc");
    org.mitre.niem.cmf.Namespace cmfNamespace = namespace.toCmf();
    assertEquals("nc", cmfNamespace.prefix());
    assertEquals("CORE", cmfNamespace.kindCode());

    Model cmfModel = new Model();
    // cmfModel.addNamespace(cmfNamespace);
    namespace.addToCmfModel(cmfModel, false, AddModelReason.REPRESENTATION, null);
    String cmfXml = CmfUtils.generateString(cmfModel);
    System.out.println(cmfXml);

  }

}
