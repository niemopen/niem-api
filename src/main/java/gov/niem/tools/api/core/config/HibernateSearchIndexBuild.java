package gov.niem.tools.api.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import gov.niem.tools.api.search.SearchService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Configuration
public class HibernateSearchIndexBuild implements ApplicationListener < ApplicationReadyEvent > {

  @Autowired
  private EntityManager em;

  @Autowired
  SearchService searchService;

  /**
   * TODO: Errors indexing Version, Namespace, Subproperty, and Facet
   */
  @Override
  @Transactional
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // searchService.runIndexer();
  }

}
