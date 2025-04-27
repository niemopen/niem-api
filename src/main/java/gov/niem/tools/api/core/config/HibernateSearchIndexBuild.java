package gov.niem.tools.api.core.config;

import gov.niem.tools.api.search.SearchService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * Builds a database index for Hibernate searches on an application event.
 */
@Configuration
public class HibernateSearchIndexBuild implements ApplicationListener<ApplicationReadyEvent> {

  @Autowired
  SearchService searchService;

  /**
   * Builds a database index for Hibernate searches on the given application event.
   * TODO: Errors indexing Version, Namespace, Subproperty, and Facet
   */
  @Override
  @Transactional
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    // searchService.runIndexer();
  }

}
