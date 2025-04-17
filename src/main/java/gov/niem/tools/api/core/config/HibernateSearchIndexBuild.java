package gov.niem.tools.api.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import gov.niem.tools.api.search.SearchService;
import jakarta.transaction.Transactional;

@Configuration
public class HibernateSearchIndexBuild implements ApplicationListener < ApplicationReadyEvent > {

  @Autowired
  SearchService searchService;

  /**
   * TODO: Errors indexing Version, Namespace, Subproperty, and Facet
   */
  @Override
  @Transactional
  public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
    // searchService.runIndexer();
  }

}
