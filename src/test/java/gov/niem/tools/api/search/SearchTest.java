package gov.niem.tools.api.search;

import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.TestData;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.version.Version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.search.engine.search.query.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests component searches.
 */
@ActiveProfiles("test")
@SpringBootTest
public class SearchTest {

  @Autowired
  SearchService searchService;

  @Autowired
  ServiceHub hub;

  Steward steward;
  Model model;
  Version version;
  Namespace namespace;

  /**
   * Add test data and create the search index.
   */
  @BeforeEach
  public void init() throws Exception {
    // Add a steward, model, version, and namespace
    steward = hub.stewards.add(TestData.Stewards.nmo());
    model = hub.models.add(TestData.Models.niem(steward));
    version = hub.versions.add(TestData.Versions.major(model, "1.0"));
    namespace = hub.namespaces.add(TestData.Namespaces.domain(version, "ext"));

    // Add properties with "arm" tokens for search testing
    hub.properties.add(namespace, "ArmedServiceCategoryAbstract");
    hub.properties.add(namespace, "ArmingDelayHoursDuration");
    hub.properties.add(namespace, "SubjectArmedDescriptionText");
    hub.properties.add(namespace, "PersonArmedIndicator");

    // Add properties with "arm" substrings for search testing
    hub.properties.add(namespace, "AbuseNeglectHarmAgeText");
    hub.properties.add(namespace, "AlarmAudibleDescriptionCode");
    hub.properties.add(namespace, "AlarmAudibleIndicator");
    hub.properties.add(namespace, "AngularMeasure");
    hub.properties.add(namespace, "ArmyPatientsQuantity");
    hub.properties.add(namespace, "FarmNumberID");

    // Add additional properties
    hub.properties.add(namespace, "PersonGivenName");
    hub.properties.add(namespace, "LocationStateName");
    hub.properties.add(namespace, "FacilityLocation");
    hub.properties.add(namespace, "ItemModelName");
    hub.properties.add(namespace, "ActivityDate");
    hub.properties.add(namespace, "VehiclePrimaryColorText");

    searchService.runIndexer();
  }

  /**
   * Test property searches by token.
   *
   * @todo Fix search property tokens test.  Currently returns no results.
   */
  @Test
  @Transactional
  @Disabled
  public void testPropertySearchTokens() {
    String[] tokens = {"arm"};
    SearchResult<Property> results = searchService.searchProperty("1.0", tokens, null, null,
        null, null, null, null, null, null, null, null);

    List<Property> properties = results.hits();
    assertTrue(properties.size() > 0);

    List<String> names = properties.stream().map(Property::getName).collect(Collectors.toList());
    System.out.println(names.toString());

    // Check tokens exist - arm, armed, arming, etc.
    assertTrue(names.contains("ArmedServiceCategoryAbstract"));
    assertTrue(names.contains("ArmingDelayHoursDuration"));
    assertTrue(names.contains("SubjectArmedDescriptionText"));
    assertTrue(names.contains("PersonArmedIndicator"));

    // Check substring matches are not returned - harm, alarm, army, farm, etc.
    assertFalse(names.contains("AbuseNeglectHarmAgeText"));
    assertFalse(names.contains("AlarmAudibleDescriptionCode"));
    assertFalse(names.contains("AlarmAudibleIndicator"));
    assertFalse(names.contains("AngularMeasure"));
    assertFalse(names.contains("ArmyPatientsQuantity"));
    assertFalse(names.contains("FarmNumberID"));
  }

  /**
   * Test property searches by substring.
   *
   * @todo Fix search property tokens test.  Currently returns no results.
   */
  @Test
  @Transactional
  @Disabled
  public void testPropertySearchSubstrings() {
    String[] substrings = {"arm"};
    SearchResult<Property> results = searchService.searchProperty("1.0", null, substrings,
        null, null, null, null, null, null, null, null, null);

    List<Property> properties = results.hits();
    assertTrue(properties.size() > 0);

    List<String> names = properties.stream().map(Property::getName).collect(Collectors.toList());
    System.out.println(names.toString());

    // Check tokens exist - arm, armed, arming, etc.
    assertTrue(names.contains("ArmedServiceCategoryAbstract"));
    assertTrue(names.contains("ArmingDelayHoursDuration"));
    assertTrue(names.contains("SubjectArmedDescriptionText"));
    assertTrue(names.contains("PersonArmedIndicator"));

    // Check non-token substring matches are also returned - harm, alarm, army, farm, etc.
    assertTrue(names.contains("AbuseNeglectHarmAgeText"));
    assertTrue(names.contains("AlarmAudibleDescriptionCode"));
    assertTrue(names.contains("AlarmAudibleIndicator"));
    assertTrue(names.contains("AngularMeasure"));
    assertTrue(names.contains("ArmyPatientsQuantity"));
    assertTrue(names.contains("FarmNumberID"));
  }

}
