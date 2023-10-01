package gov.niem.tools.api.search;

import java.util.Arrays;

import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.niem.tools.api.core.exceptions.NoContentException;
import gov.niem.tools.api.db.ServiceHub;
import gov.niem.tools.api.db.model.Model;
import gov.niem.tools.api.db.namespace.Namespace;
import gov.niem.tools.api.db.property.Property;
import gov.niem.tools.api.db.steward.Steward;
import gov.niem.tools.api.db.type.Type;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SearchService {

  @Autowired
  EntityManager em;

  @Autowired
  ServiceHub hub;

  public final static Integer LIMIT_DEFAULT = 100;

  @SneakyThrows
  @Transactional
  public void runIndexer() {
    log.info("Initializing indexes...");
    SearchSession searchSession = Search.session(em);
    MassIndexer indexer = searchSession.massIndexer(
      Steward.class,
      Model.class,
      Property.class,
      Type.class
    )
    .idFetchSize(150)
    .batchSizeToLoadObjects(25)
    .threadsToLoadObjects(6)
    ;

    try {
      indexer.startAndWait();
    }
    catch (InterruptedException exception) {
      log.warn("Failed to load data from database");
      Thread.currentThread().interrupt();
    }
    log.info("Indexer completed");
  }

  public SearchResult<Property> searchProperty(
    String niemVersionNumber,
    String[] tokens,
    String[] substrings,
    String[] prefixes,
    String[] types,
    String[] groups,
    String[] stewards,
    String[] models,
    Boolean isAbstract,
    Boolean isElement,
    Namespace.Category[] namespaceCategories,
    Integer offset,
    Integer limit
  ) {

    SearchSession searchSession = Search.session(em);

    String baseNIEM = niemVersionNumber == null ? hub.models.currentNiemVersion().getVersionNumber() : niemVersionNumber;

    // Make sure limit is within valid range
    limit = adjustLimit(limit);

    // Log parameters
    log.info(String.format("SEARCH properties based on NIEM %s", baseNIEM));
    printParameter("token", tokens);
    printParameter("term", substrings);
    printParameter("prefix", prefixes);
    printParameter("type", types);
    printParameter("group", groups);
    printParameter("steward", stewards);
    printParameter("model", models);
    printParameter("nsCategory", namespaceCategories);
    printParameter("isAbstract", isAbstract);
    printParameter("isElement", isElement);

    SearchResult<Property> result = searchSession
    .search(Property.class)
    // .select(f -> f.field("name_keyword", String.class))
    .where(f -> f.and().with( and -> {

      // Base NIEM version number
      and.add(f.match().fields("namespace.version.niemVersion.versionNumber").matching(baseNIEM));

      if (tokens != null) {
        // Search tokens across name, definition, and keywords fields
        and.add(f.terms().fields("name", "keywords").boost(3f).fields("definition").matchingAll(Arrays.asList(tokens)));
      }

      if (substrings != null) {
        // Search terms across name, definition, and keywords fields
        and.add(f.simpleQueryString().fields("name_substring", "keywords").boost(3f).fields("definition").matching(String.join("+", substrings)));
      }

      if (prefixes != null) {
        // Search prefixes
        and.add(f.simpleQueryString().field("namespace.prefix").matching(String.join("|", prefixes)));
      }

      // TODO: Work on types
      if (types != null) {
        // Search property type names
        // and.add(f.terms().field("type.name").matchingAny(Arrays.asList(types)));
        and.add(f.simpleQueryString().field("type.name").matching(String.join("|", types)));
      }

      // TODO: groups

      // TODO: models
      // if (models != null) {
        // and.add(f.simpleQueryString().field("namespace.version.model.shortName").matching(String.join("|", models)));
        // and.add(f.match().field("namespace.version.model.shortName").matching(models[0]));
      // }

      // TODO: stewards

      // Search on value of category field if isAbstract or isElement not null
      if (isAbstract == null && isElement == null) {
        // Do nothing
      }
      else if (isAbstract != null && isAbstract == true && isElement != null && isElement == false) {
        // Invalid combination: a property cannot be an abstract attribute
        throw new NoContentException();
      }
      else if (isAbstract != null && isAbstract == true) {
        // Abstract element
        and.add(f.match().field("category").matching(Property.Category.abstract_element));
      }
      else if (isAbstract != null && isAbstract == false && isElement == null) {
        // Concrete element or attribute
        and.add(f.simpleQueryString().field("category").matching(Property.Category.attribute + "|" + Property.Category.element));
      }
      else if (isElement != null && isElement == true) {
        // Element
        and.add(f.match().field("category").matching(Property.Category.element));
      }
      else if (isElement != null && isElement == false) {
        // Attribute
        and.add(f.match().field("category").matching(Property.Category.attribute));
      }

    }))
    .sort(f -> f.score().then().field("name_keyword").then().field("namespace.prefix"))
    .fetch(offset, limit);

    log.info(String.format("Search runtime: [%s]", result.took()));

    return result;
  }

  public SearchResult<Type> searchType(
      String niemVersionNumber,
      String[] tokens,
      String[] substrings,
      String[] prefixes,
      Integer offset,
      Integer limit) {

    SearchSession searchSession = Search.session(em);

    String baseNIEM = niemVersionNumber == null ? hub.models.currentNiemVersion().getVersionNumber()
        : niemVersionNumber;

    // Make sure limit is within valid range
    limit = adjustLimit(limit);

    // Log parameters
    log.info(String.format("SEARCH types based on NIEM %s", baseNIEM));
    printParameter("token", tokens);
    printParameter("term", substrings);
    printParameter("prefix", prefixes);

    SearchResult<Type> result = searchSession
        .search(Type.class)
        // .select(f -> f.field("name_keyword", String.class))
        .where(f -> f.and().with(and -> {

          // Base NIEM version number
          and.add(f.match().fields("namespace.version.niemVersion.versionNumber").matching(baseNIEM));

          if (tokens != null) {
            // Search tokens across name, definition, and keywords fields
            and.add(
                f.terms().fields("name").boost(3f).fields("definition").matchingAll(Arrays.asList(tokens)));
          }

          if (substrings != null) {
            // Search terms across name, definition, and keywords fields
            and.add(f.simpleQueryString().fields("name_substring").boost(3f).fields("definition")
                .matching(String.join("+", substrings)));
          }

          if (prefixes != null) {
            // Search prefixes
            and.add(f.simpleQueryString().field("namespace.prefix").matching(String.join("|", prefixes)));
          }

        }))
        .sort(f -> f.score().then().field("name_keyword").then().field("namespace.prefix"))
        .fetch(offset, limit);

    log.info(String.format("Search runtime: [%s]", result.took()));

    return result;
  }

  public void setResponseHeaders(HttpServletResponse response, SearchResult<? extends Object> searchResult, Integer offset, Integer limit, String requestUrl) {

    Integer total = (int) searchResult.total().hitCount();
    response.setHeader("X-Total-Count", total.toString());

    if (total == 0) {
      throw new NoContentException();
    }

    // Ensure limit is within valid range
    limit = adjustLimit(limit);

    // Remove offset query parameter from request URL
    String url = offset == null ? requestUrl : requestUrl.replaceAll("&offset=\\d+", "");

    if (offset == null) {
      offset = 0;
    }

    // No offset
    if (total > limit && offset != 0) {
      response.setHeader("X-Page-First", url);
    }

    // Return 0 offset or null if
    Integer prev = offset < limit ? null : offset - limit;
    if (prev != null) {
      response.setHeader("X-Page-Prev", url + "&offset=" + prev);
    }

    Integer next = offset >= total - limit ? null : offset + limit;
    if (next != null) {
      response.setHeader("X-Page-Next", url + "&offset=" + next);
    }

    // Return last page of results
    Integer last = total - limit;
    if (total > limit && offset < total - limit) {
      response.setHeader("X-Page-Last", url + "&offset=" + last);
    }

  }

  private Integer adjustLimit(Integer limit) {
    if (limit == null || limit < 1 || limit > LIMIT_DEFAULT) {
      return LIMIT_DEFAULT;
    }
    return limit;
  }

  private void printParameter(String name, String value) {
    if (value != null) {
      log.info(String.format("--%s: [%s]", name, value));
    }
  }

  private void printParameter(String name, String[] values) {
    if (values != null) {
      printParameter(name, String.join(", ", values));
    }
  }

  private void printParameter(String name, Boolean value) {
    if (value != null) {
      printParameter(name, value.toString());
    }
  }

  private void printParameter(String name, Namespace.Category[] values) {
    if (values != null) {
      printParameter(name, values.toString());
    }
  }

}
