package com.adaptris.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * A {@linkplain Set} of <code>KeyValuePair</code> instances.
 * </p>
 * 
 * @config key-value-pair-set
 */
@XStreamAlias("key-value-pair-set")
public class KeyValuePairSet extends KeyValuePairBag implements Set<KeyValuePair>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 2013111201L;

  @XStreamImplicit(itemFieldName = "key-value-pair")
  private Set<KeyValuePair> set;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public KeyValuePairSet() {
    super();
    set = new HashSet<KeyValuePair>();
  }

  public KeyValuePairSet(Collection<KeyValuePair> pairs) {
    this();
    addAll(pairs);
  }

  public KeyValuePairSet(Properties properties) {
    this();
    addAll(properties);
  }

  @Override
  public Set<KeyValuePair> getKeyValuePairs() {
    return set;
  }

  @Override
  public void addKeyValuePair(KeyValuePair pair) {
    if (pair == null) {
      throw new IllegalArgumentException();
    }
    if (getKeyValuePairs().contains(pair)) {
      getKeyValuePairs().remove(pair);
    }
    add(pair);
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && o instanceof Set;
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * The behaviour of this method is the same as
   * {@link AbstractSet#removeAll(Collection)}
   * </p>
   *
   * @see AbstractSet#removeAll(Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    boolean modified = false;

    if (size() > c.size()) {
      for (Iterator<?> i = c.iterator(); i.hasNext();) {
        modified |= remove(i.next());
      }
    }
    else {
      for (Iterator<?> i = iterator(); i.hasNext();) {
        if (c.contains(i.next())) {
          i.remove();
          modified = true;
        }
      }
    }
    return modified;
  }
}
