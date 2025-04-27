package gov.niem.tools.api.search;

import gov.niem.tools.api.db.property.Property;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.search.engine.search.query.SearchResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests component searches.
 */
@SpringBootTest
public class SearchTest {

  @Autowired
  SearchService searchService;

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
    SearchResult<Property> results = searchService.searchProperty("6.0", tokens, null, null,
        null, null, null, null, null, null, null, null, null);

    List<Property> properties = results.hits();
    assertTrue(properties.size() > 0);

    List<String> qnames = properties.stream().map(Property::getQname).collect(Collectors.toList());
    System.out.println(qnames.toString());

    // Check tokens exist - arm, armed, arming, etc.
    assertTrue(qnames.contains("mo:ArmedServiceCategoryAbstract"));
    assertTrue(qnames.contains("usmtf:ArmingDelayHoursDuration"));
    assertTrue(qnames.contains("j:SubjectArmedDescriptionText"));
    assertTrue(qnames.contains("nc:PersonArmedIndicator"));

    // Check substring matches are not returned - harm, alarm, army, farm, etc.
    assertFalse(qnames.contains("hs:AbuseNeglectHarmAgeText"));
    assertFalse(qnames.contains("em:AlarmAudibleDescriptionCode"));
    assertFalse(qnames.contains("cbrn:AlarmAudibleIndicator"));
    assertFalse(qnames.contains("nc:AngularMeasure"));
    assertFalse(qnames.contains("usmtf:ArmyPatientsQuantity"));
    assertFalse(qnames.contains("ag:FarmNumberID"));
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
    SearchResult<Property> results = searchService.searchProperty("6.0", null, substrings,
        null, null, null, null, null, null, null, null, null, null);

    List<Property> properties = results.hits();
    assertTrue(properties.size() > 0);

    List<String> qnames = properties.stream().map(Property::getQname).collect(Collectors.toList());
    System.out.println(qnames.toString());

    // Check tokens exist - arm, armed, arming, etc.
    assertTrue(qnames.contains("mo:ArmedServiceCategoryAbstract"));
    assertTrue(qnames.contains("usmtf:ArmingDelayHoursDuration"));
    assertTrue(qnames.contains("j:SubjectArmedDescriptionText"));
    assertTrue(qnames.contains("nc:PersonArmedIndicator"));

    // Check non-token substring matches are also returned - harm, alarm, army, farm, etc.
    assertTrue(qnames.contains("hs:AbuseNeglectHarmAgeText"));
    assertTrue(qnames.contains("em:AlarmAudibleDescriptionCode"));
    assertTrue(qnames.contains("cbrn:AlarmAudibleIndicator"));
    assertTrue(qnames.contains("nc:AngularMeasure"));
    assertTrue(qnames.contains("usmtf:ArmyPatientsQuantity"));
    assertTrue(qnames.contains("ag:FarmNumberID"));
  }

}
