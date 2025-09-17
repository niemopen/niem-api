package gov.niem.tools.api.search;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.sort.SearchSort;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.massindexing.MassIndexingMonitor;
import org.hibernate.search.mapper.pojo.massindexing.impl.PojoMassIndexingLoggingMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Operations to support database searches.
 */
@Component
@Log4j2
public class SearchService {

  @Autowired
  EntityManager em;

  @Autowired
  ServiceHub hub;

  public static final Integer LIMIT_DEFAULT = 100;

  /**
   * Initialize database indexes.  Must be run after application is restarted or
   * searches will return empty results.
   */
  @SneakyThrows
  @Transactional
  @EventListener(ApplicationReadyEvent.class)
  public void runIndexer() {
    log.info("Initializing indexes...");
    MassIndexingMonitor monitor = new PojoMassIndexingLoggingMonitor(10000);
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
        .monitor(monitor);

    indexer.purgeAllOnStart(true);

    try {
      indexer.startAndWait();
    }
    catch (InterruptedException exception) {
      log.warn("Failed to create indexes to support search operations");
      Thread.currentThread().interrupt();
    }
    log.info("Indexer completed");
  }

  /**
   * Defines options for sorting search results.
   */
  public enum SortOrder {

    /**
     * Sort by weighted score, then name, then prefix.
     */
    score_name,

    /**
     * Sort by weighted score, then qname.
     */
    score_qname,

    /**
     * Sort by namespace rank (Core first, then domains, etc.), then qname.
     */
    rank_qname,

    /**
     * Sort by namespace rank (Core first, then domains, etc.), then name, then prefix.
     */
    rank_name,

    /**
     * Sort by name, then prefix.
     */
    name,

    /**
     * Sort by qname.
     */
    qname
  }

  /**
   * Search properties in the database.
   */
  public SearchResult<Property> searchProperty(
      String niemVersionNumber,
      String[] tokens,
      String[] substrings,
      String[] prefixes,
      String[] types,
      String[] stewards,
      String[] models,
      Boolean isAbstract,
      Boolean isElement,
      Namespace.Category[] namespaceCategories,
      SortOrder sortOrder,
      Integer page,
      Integer limit
  ) {

    String baseNiem = niemVersionNumber == null
        ? hub.models.currentNiemVersion().getVersionNumber()
        : niemVersionNumber;

    // Make sure limit is within valid range
    limit = adjustLimit(limit);

    // Log parameters
    log.info(String.format("SEARCH properties based on NIEM %s", baseNiem));
    printParameter("token", tokens);
    printParameter("term", substrings);
    printParameter("prefix", prefixes);
    printParameter("type", types);
    printParameter("steward", stewards);
    printParameter("model", models);
    printParameter("namespaceCategory", namespaceCategories);
    printParameter("isAbstract", isAbstract);
    printParameter("isElement", isElement);

    // Set up prefix list
    List<String> prefixList = new ArrayList<>();

    if (prefixes != null) {
      Collections.addAll(prefixList, prefixes);
    }

    // Process namespace prefixes in term arrays
    processQnames(tokens, prefixList);
    processQnames(substrings, prefixList);

    // Process namespace prefixes in type term array
    List<String> typePrefixList = new ArrayList<>();
    processQnames(types, typePrefixList);

    SearchSession searchSession = Search.session(em);

    SearchResult<Property> result = searchSession
        .search(Property.class)
        // .select(f -> f.field("name_string", String.class))
        .where(f -> f.and().with(and -> {

          // Base NIEM version number
          and.add(f.match().fields("namespace.version.niemVersion.versionNumber")
              .matching(baseNiem));

          if (tokens != null) {
            // Search tokens across name, definition, and keywords fields
            List<String> criteria = processTokens(tokens);
            and.add(f.terms().fields("name_tokens", "keywords")
                .boost(3f).fields("definition").matchingAll(criteria));
          }

          if (substrings != null) {
            // Search terms across name, definition, and keywords fields
            String criteria = String.join("+", substrings).toLowerCase();
            and.add(f.simpleQueryString().fields("name_substring", "keywords")
                .boost(3f).fields("definition").matching(criteria));
          }

          if (!prefixList.isEmpty()) {
            // Search prefixes
            String criteria = String.join("|", prefixList).toLowerCase();
            and.add(f.simpleQueryString().field("namespace.prefix").matching(criteria));
          }

          if (types != null && types.length > 0) {
            // Search property type names
            List<String> criteria = processTokens(types);
            and.add(f.terms().field("type.name_substring").matchingAll(criteria));
            // String criteria = String.join("+", types).toLowerCase();
            // and.add(f.simpleQueryString().field("type.name_substring").matching(criteria));
          }

          // if (!typePrefixList.isEmpty()) {
          //   // Search property type prefixes
          //   String criteria = String.join("|", typePrefixList);
          //   and.add(f.simpleQueryString().field("type.prefix").matching(criteria));
          // }

          if (namespaceCategories != null && namespaceCategories.length > 0) {
            // Search property namespace categories
            String criteria = enumsAsStrings(namespaceCategories, "|");
            and.add(f.simpleQueryString().field("namespace.category").matching(criteria));
          }

          // TODO: models
          // if (models != null) {
          //   and.add(f.simpleQueryString().field("namespace.version.model.shortName")
          //       .matching(String.join("|", models)));
          //   and.add(f.match().field("namespace.version.model.shortName").matching(models[0]));
          // }

          // TODO: stewards

          // Search on value of category field if isAbstract or isElement not null
          if (isAbstract == null && isElement == null) {
            // Do nothing
          }
          else if (isAbstract != null && isAbstract == true
              && isElement != null && isElement == false) {
            // Invalid combination: a property cannot be an abstract attribute
            throw new NoContentException();
          }
          else if (isAbstract != null && isAbstract == true) {
            // Abstract element
            and.add(f.match().field("category").matching(Property.Category.abstract_element));
          }
          else if (isAbstract != null && isAbstract == false && isElement == null) {
            // Concrete element or attribute
            and.add(f.simpleQueryString().field("category")
                .matching(Property.Category.attribute + "|" + Property.Category.element));
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
        .sort(this.getPropertySort(searchSession.scope(Property.class), sortOrder))
        .fetch(page * limit, limit);

    log.info(String.format("Search runtime: [%s]", result.took()));

    return result;
  }

  /**
   * Search types in the database.
   */
  public SearchResult<Type> searchType(
      String niemVersionNumber,
      String[] tokens,
      String[] substrings,
      String[] prefixes,
      Type.Category category,
      Namespace.Category[] namespaceCategories,
      SortOrder sortOrder,
      Integer page,
      Integer limit) {

    String baseNiem = niemVersionNumber == null
        ? hub.models.currentNiemVersion().getVersionNumber()
        : niemVersionNumber;

    // Make sure limit is within valid range
    limit = adjustLimit(limit);

    // Log parameters
    log.info(String.format("SEARCH types based on NIEM %s", baseNiem));
    printParameter("token", tokens);
    printParameter("term", substrings);
    printParameter("prefix", prefixes);
    printParameter("namespaceCategory", namespaceCategories);

    if (category != null) {
      printParameter("category", category.name());
    }

    // Set up prefix list
    List<String> prefixList = new ArrayList<>();

    if (prefixes != null) {
      Collections.addAll(prefixList, prefixes);
    }

    // Process namespace prefixes in term arrays
    processQnames(tokens, prefixList);
    processQnames(substrings, prefixList);

    SearchSession searchSession = Search.session(em);

    SearchResult<Type> result = searchSession
        .search(Type.class)
        .where(f -> f.and().with(and -> {

          // Base NIEM version number
          and.add(f.match().fields("namespace.version.niemVersion.versionNumber")
              .matching(baseNiem));

          if (tokens != null) {
            // Search tokens across name, definition, and keywords fields
            List<String> criteria = processTokens(tokens);
            and.add(f.terms().fields("name_tokens")
                .boost(3f).fields("definition").matchingAll(criteria));
          }

          if (substrings != null) {
            // Search terms across name, definition, and keywords fields
            String criteria = String.join("+", substrings).toLowerCase();
            and.add(f.simpleQueryString().field("name_substring").boost(3f).fields("definition")
                .matching(criteria));
          }

          if (!prefixList.isEmpty()) {
            // Search prefixes
            and.add(f.simpleQueryString().field("namespace.prefix")
                .matching(String.join("|", prefixList)));
          }

          if (category != null) {
            and.add(f.simpleQueryString().field("category").matching(category.name()));
          }

          if (namespaceCategories != null && namespaceCategories.length > 0) {
            // Search property namespace categories
            String criteria = enumsAsStrings(namespaceCategories, "|");
            and.add(f.simpleQueryString().field("namespace.category").matching(criteria));
          }

        }))
        .sort(this.getTypeSort(searchSession.scope(Type.class), sortOrder))
        .fetch(page * limit, limit);

    log.info(String.format("Search runtime: [%s]", result.took()));

    return result;
  }

  /**
   * Set the sort order based on the given criteria.
   */
  private SearchSort getPropertySort(SearchScope<Property> scope, SortOrder order) {

    if (order == null) {
      // Default sort
      return scope.sort()
          .field("namespace.prefix").then()
          .field("name_sort").toSort();
    }

    switch (order) {
      case name:
        return scope.sort()
            .field("name_sort").then()
            .field("namespace.prefix").toSort();
      case qname:
        return scope.sort()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
      case rank_name:
        return scope.sort()
            .field("namespace.rank").then()
            .field("name_sort").then()
            .field("namespace.prefix").toSort();
      case rank_qname:
        return scope.sort()
            .field("namespace.rank").then()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
      case score_name:
        return scope.sort()
            .score().then()
            .field("name_sort").then()
            .field("namespace.prefix").toSort();
      case score_qname:
        return scope.sort()
            .score().then()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
      default:
        return scope.sort()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
    }
  }

  /**
   * Set the sort order based on the given criteria.
   *
   * @todo Refactor property and type sort methods into a single method. Was throwing
   *      an error using SearchScope for a Component so temporarily created custom methods
   *      for each.
   */
  private SearchSort getTypeSort(SearchScope<Type> scope, SortOrder order) {

    if (order == null) {
      // Default sort
      return scope.sort()
          .field("namespace.prefix").then()
          .field("name_sort").toSort();
    }

    switch (order) {
      case name:
        return scope.sort()
            .field("name_sort").then()
            .field("namespace.prefix").toSort();
      case qname:
        return scope.sort()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
      case rank_name:
        return scope.sort()
            .field("namespace.rank").then()
            .field("name_sort").then()
            .field("namespace.prefix").toSort();
      case rank_qname:
        return scope.sort()
            .field("namespace.rank").then()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
      case score_name:
        return scope.sort()
            .score().then()
            .field("name_sort").then()
            .field("namespace.prefix").toSort();
      case score_qname:
        return scope.sort()
            .score().then()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
      default:
        return scope.sort()
            .field("namespace.prefix").then()
            .field("name_sort").toSort();
    }

  }

  /**
   * Set search response headers to include the total number of hits, the
   * current number of results, and pagination information.
   */
  public void setResponseHeaders(HttpServletResponse response,
      SearchResult<? extends Object> searchResult, Integer page, Integer limit,
      String requestUrl) {

    Integer total = (int) searchResult.total().hitCount();
    response.setHeader("X-Total-Count", total.toString());

    if (total == 0) {
      throw new NoContentException();
    }

    // Ensure limit is within valid range
    limit = adjustLimit(limit);

    // Remove offset query parameter from request URL
    String url = page == null ? requestUrl : requestUrl.replaceAll("&page=\\d+", "");

    if (page == null) {
      page = 0;
    }

    if (page > 0) {
      // Return link to previous page and first page of search results
      response.setHeader("X-Page-Prev", url + "&page=" + (page - 1));
      response.setHeader("X-Page-First", url);
    }

    Integer lastPage = (int) Math.ceil(total / limit) + 1;
    if (page < lastPage) {
      response.setHeader("X-Page-Next", url + "&page=" + (page + 1));
      response.setHeader("X-Page-Last", url + "&page=" + lastPage);
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
      printParameter(name, enumsAsStrings(values, ", "));
    }
  }

  /**
   * Converts the given enum array to a string separated by the given delimiter.
   */
  private String enumsAsStrings(Namespace.Category[] enums, String delimiter) {
    return Arrays.stream(enums).map(Enum::name).collect(Collectors.joining(delimiter));
  }

  /**
   * Updates the given arrays.  For each term in the terms array, checks to see
   * if it is a qualified name.  If so, removes the prefix and adds it to the
   * prefixes array if not already present.
   */
  private void processQnames(String[] terms, List<String> prefixes) {
    if (terms == null) {
      return;
    }

    for (int i = 0; i < terms.length; i++) {
      String term = terms[i];
      if (term.contains(":")) {
        // Add prefix if unique
        String prefix = term.split(":")[0];
        if (!prefixes.contains(prefix)) {
          prefixes.add(prefix);
        }

        // Remove the prefix from the term entry
        terms[i] = term.split(":")[1];
      }
    }
  }

  /**
   * Splits a token array on camel casing.
   */
  private List<String> processTokens(String[] tokens) {
    String text = String.join(" ", tokens);
    text = text.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    System.out.println("SPLIT TOKENS " + text);
    return Arrays.asList(text.toLowerCase().split(" "));
  }

}
