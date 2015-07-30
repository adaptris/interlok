package com.adaptris.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A container class for handling a {@link Collection} of {@link MetadataElement} instance.
 ** <p>
 * In the adapter configuration file this class is aliased as <b>metadata-collection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author amcgrath
 *
 */
@XStreamAlias("metadata-collection")
public class MetadataCollection extends ArrayList<MetadataElement> {

  /**
   * For backward compatible serialisation
   */
  private static final long serialVersionUID = 20120319103110L;

  public MetadataCollection() {
    super();
  }

  /**
   * Constructor. Allows you to create an instance of <code>MetadataCollection</code> from a <code>Set</code>
   *
   * @param elements a
   */
  public MetadataCollection(Set<MetadataElement> elements) {
    super();
    for (MetadataElement e : elements) {
      add(e);
    }
  }

  /**
   * Constructor. Allows you to create an instance of <code>MetadataCollection</code> from another instance of
   * <code>MetadataCollection</code>, which will result in a direct copy.
   *
   * @param metadataCollection collection of metadata.
   */
  public MetadataCollection(MetadataCollection metadataCollection) {
    super();

    for (MetadataElement element : metadataCollection) {
      add(element);
    }
  }

  /**
   * Will create an return a new <code>Set</code> from this <code>MetadataCollection</code>
   *
   * @return a set of metadata elements.
   */
  public Set<MetadataElement> toSet() {
    Set<MetadataElement> resultSet = new HashSet<MetadataElement>(this);
    return resultSet;
  }

  /**
   * Simple utility method that will scan the <code>MetadataElement</code>'s and will check the each key equals the supplied key.
   * Note, this method id case sensitive.
   *
   * @param key the key to search for.
   * @return true if the collection contains this key.
   */
  public boolean containsKey(String key) {
    boolean result = false;
    for (MetadataElement element : this) {
      if (element.getKey().equals(key)) {
        result = true;
        break;
      }
    }
    return result;
  }
}
