/* $Id: KeyValuePairSet.java,v 1.4 2008/08/07 12:57:45 lchan Exp $ */
package com.adaptris.util;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * A Collection of of {@linkplain KeyValuePair} instances.
 * </p>
 */
public abstract class KeyValuePairBag extends AbstractCollection<KeyValuePair> {

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public KeyValuePairBag() {
  }

  /**
   * <p>
   * Add a KeyValuePair to the collection
   * </p>
   *
   * @param pair the <code>KeyValuePair</code> to add, may not be null
   */
  public abstract void addKeyValuePair(KeyValuePair pair);

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(KeyValuePair pair) {
    return getKeyValuePairs().add(pair);
  }

  /**
   * Returns the underlying {@linkplain Collection} of {@linkplain KeyValuePair}
   * instances
   *
   * @return underlying <code>Set</code> of <code>KeyValuePair</code>s
   */
  public abstract Collection<KeyValuePair> getKeyValuePairs();

  /**
   *
   * Set the underlying {@linkplain Collection} of {@linkplain KeyValuePair}
   * instances
   *
   * @param list the collection
   */
  public void setKeyValuePairs(Collection<KeyValuePair> list) {
    getKeyValuePairs().clear();
    getKeyValuePairs().addAll(list);
  }

  /**
   * Return the first {@linkplain KeyValuePair} that matches the given key.
   *
   * @param key the key to look up
   * @return the {@linkplain KeyValuePair} for the passed key or null if none
   *         exists
   */
  public KeyValuePair getKeyValuePair(String key) {
    // Yes this relies on the on a KeyValuePair being semantically
    // equal based on it's key, but that is intentional by design.
    return getKeyValuePair(new KeyValuePair(key, ""));
  }

  /**
   * Remove the first {@linkplain KeyValuePair} from the underlying collection.
   *
   * @param kp the {@linkplain KeyValuePair} to remove.
   */
  public void removeKeyValuePair(KeyValuePair kp) {
    if (kp != null) {
      getKeyValuePairs().remove(kp);
    }
  }

  /**
   * Convenience method to remove a {@linkplain KeyValuePair} from the
   * underlying collection.
   *
   * @param key the key.
   */
  public void removeKeyValuePair(String key) {
    removeKeyValuePair(new KeyValuePair(key, ""));
  }

  /**
   * Does the underlying set contain this {@linkplain KeyValuePair}
   *
   * @param kp the keyvalue pair.
   * @return true
   */
  public boolean contains(KeyValuePair kp) {
    return getKeyValuePairs().contains(kp);
  }

  private KeyValuePair getKeyValuePair(KeyValuePair k) {
    List<Object> list = Arrays.asList(getKeyValuePairs().toArray());
    if (list.contains(k)) {
      return (KeyValuePair) list.get(list.indexOf(k));
    }
    return null;
  }

  /**
   * Convenience method for returning the value associated with a given key.
   * <p>
   * Returns the <code>String</code> value associated with the passed key or
   * null if none exists.
   * </p>
   *
   * @param key the key to look up
   * @return the <code>String</code> value associated with the passed key or
   *         null if none exists
   */
  public String getValue(String key) {
    KeyValuePair k = this.getKeyValuePair(key);
    return k != null ? k.getValue() : null;
  }

  /**
   * <p>
   * Return the first value associated with the passed key, ignoring the case of
   * the key. This may legitmately contain two keys "AAA" and "aaa". Calling
   * <code>getValueIgnoringKeyCase(aaa)</code> may return the value associated
   * with either of these keys.
   * </p>
   *
   * @param key the key to look up
   * @return the first assoicated value.
   */
  public String getValueIgnoringKeyCase(String key) {
    String result = null;
    for (KeyValuePair kp : getKeyValuePairs()) {
      if (key != null && key.equalsIgnoreCase(kp.getKey())) {
        result = kp.getValue();
        break;
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return getKeyValuePairs().size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return asProperties(this).toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<KeyValuePair> iterator() {
    return getKeyValuePairs().iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int h = 0;
    for (KeyValuePair k : this) {
      h += k.hashCode();
    }
    return h;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof Collection)) {
      return false;
    }
    return size() == ((Collection) o).size() && containsAll((Collection) o);
  }

  /**
   * Convert a KeyValuePairSet into a Properties object.
   *
   * @param kvps the key value pair set to convert.
   * @return a Properties object containing the key and value pairs.
   */
  public static Properties asProperties(KeyValuePairBag kvps) {
    Properties result = new Properties();
    for (KeyValuePair kvp : kvps.getKeyValuePairs()) {
      result.setProperty(kvp.getKey(), kvp.getValue());
    }
    return result;
  }

  /**
   * Convenience method to add all the associated properties to this collection
   * @param p collection of properties to add.
   */
  public void addAll(Properties p) {
    for (Map.Entry e : p.entrySet()) {
      if (e.getKey() != null) {
        addKeyValuePair(new KeyValuePair(e.getKey().toString(), e.getValue() != null ? e.getValue().toString() : null));
      }
    }
  }

  /**
   * Convenience method to add the associated map to this collection
   * 
   * @param map the map to add.
   */
  public void addAll(Map<String, String> map) {
    for (Map.Entry<String, String> e : map.entrySet()) {
      if (e.getKey() != null) {
        addKeyValuePair(new KeyValuePair(e.getKey(), e.getValue() != null ? e.getValue() : null));
      }
    }
  }

}
