package gov.niem.tools.api.db.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;

/**
 * Adds reusable methods for entities that can be versioned: Version, Namespace, Property, etc.
 *
 * @param <T> A class for a kind of entity that that supports versioning and migration rules to
 *     create previous and next links between releases, such as Version, Namespace, Property,
 *     Type, or Facet.
 */
@MappedSuperclass
@Audited
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class BaseVersionedEntity<T extends BaseVersionedEntity<T>>
    extends BaseModelEntity {

  /**
   * Corresponding entity mapped from the previous version.
   */
  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prev_id", referencedColumnName = "id")
  private T prev;

  /**
   * Corresponding entity mapped from the next version.
   */
  @JsonIgnore
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "next_id", referencedColumnName = "id")
  private T next;

  /**
   * Corresponding entity from the previous version of the model.
   * The Hibernate proxy (from lazy loading) is initialized.
   */
  @SuppressWarnings("unchecked")
  public T getPrev() {
    T prev = this.prev;
    if (prev instanceof HibernateProxy) {
      prev = (T) Hibernate.unproxy(prev);
    }
    return prev;
  }

  /**
   * Corresponding entity from the next version of the model.
   * The Hibernate proxy (from lazy loading) is initialized.
   */
  @SuppressWarnings("unchecked")
  public T getNext() {
    T next = this.next;
    if (next instanceof HibernateProxy) {
      next = (T) Hibernate.unproxy(next);
    }
    return next;
  }

}
