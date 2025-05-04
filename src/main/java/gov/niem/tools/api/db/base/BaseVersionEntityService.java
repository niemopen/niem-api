package gov.niem.tools.api.db.base;

import gov.niem.tools.api.core.exceptions.NoContentException;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class the provides additional database operations for data
 * model items that are versioned (e.g., versions, namespaces, properties, etc.)
 */
public abstract class BaseVersionEntityService<U extends BaseVersionedEntity<U>>
    extends BaseEntityService<U> {

  /**
   * Get the corresponding entity from the previous version, if available
   * and linked via migration rules.
   *
   * @param includePreRelease - True to return the result from a pre-release version if
   *     that is the immediate predecessor; false to iterate until an official version is
   *     reached.  (NOT CURRENTLY IMPLEMENTED)
   */
  public U getPrev(U object, boolean includePreRelease) throws NoContentException  {
    // TODO: Include support for iterating over pre-releases to the previous version
    U prev = object.getPrev();
    if (prev == null) {
      throw new NoContentException();
    }
    return prev;
  }

  /**
   * Get the corresponding entity from the next version, if available
   * and linked via migration rules.
   *
   * @param includePreRelease - True to return the result from a pre-release version if
   *     that is the immediate predecessor; false to iterate until an official version is
   *     reached.  (NOT CURRENTLY IMPLEMENTED)
   */
  public U getNext(U object, boolean includePreRelease) throws NoContentException {
    // TODO: Include support for iterating over pre-releases to the next version
    U next =  object.getNext();
    if (next == null) {
      throw new NoContentException();
    }
    return next;
  }

  /**
   * Get the full migration history for the given entity as available
   * from the migration rules.
   *
   * @param includePreRelease - True to return the result from a pre-release version if
   *     that is the immediate predecessor; false to iterate until an official version is
   *     reached.  (NOT CURRENTLY IMPLEMENTED)
   */
  public List<U> getHistory(U object, boolean includePreRelease) {
    List<U> history = new LinkedList<>();

    U current = object;

    // Load prior versions of the entity, with the oldest going to the front of the list
    do {
      try {
        U prev = getPrev(current, includePreRelease);
        history.add(0, prev);
        current = prev;
      }
      catch (NoContentException exception) {
        break;
      }
    } while (true);

    // Add the current version of the entity
    history.add(object);

    // Load subsequent versions of the entity
    current = object;
    do {
      try {
        U next = getNext(current, includePreRelease);
        history.add(next);
        current = next;
      }
      catch (NoContentException exception) {
        break;
      }
    } while (true);

    return history;
  }

}
