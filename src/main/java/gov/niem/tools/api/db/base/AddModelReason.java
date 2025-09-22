package gov.niem.tools.api.db.base;

/**
 * Identifies why a component was added to a CMF model, either directly due to a task
 * like migration or transformation, or indirectly as a required dependency.
 */
public enum AddModelReason {
  MIGRATION,
  DEPENDENCY,
  TRANSFORMATION,
  REPRESENTATION,
  SEARCH
}
