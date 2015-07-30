/* $Id: KeyValuePairSet.java,v 1.4 2008/08/07 12:57:45 lchan Exp $ */
package com.adaptris.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * An {@link List} of {@linkplain KeyValuePair} instances.
 * </p>
 * 
 * @config key-value-pair-list
 */
@XStreamAlias("key-value-pair-list")
public class KeyValuePairList extends KeyValuePairBag implements List<KeyValuePair> {

  @XStreamImplicit(itemFieldName = "key-value-pair")
  private List<KeyValuePair> list;

  public KeyValuePairList() {
    super();
    list = new ArrayList<KeyValuePair>();
  }

  public KeyValuePairList(Collection<KeyValuePair> pairs) {
    this();
    addAll(pairs);
  }

  @Override
  public List<KeyValuePair> getKeyValuePairs() {
    return list;
  }

  @Override
  public void addKeyValuePair(KeyValuePair pair) {
    if (pair == null) {
      throw new IllegalArgumentException();
    }
    add(pair);
  }

  /**
   * {@inheritDoc}
   */
  public void add(int index, KeyValuePair element) {
    getKeyValuePairs().add(index, element);
  }

  /**
   * {@inheritDoc}
   */
  public boolean addAll(int index, Collection<? extends KeyValuePair> c) {
    return getKeyValuePairs().addAll(index, c);
  }

  /**
   * {@inheritDoc}
   */
  public KeyValuePair get(int index) {
    return getKeyValuePairs().get(index);
  }

  /**
   * {@inheritDoc}
   */
  public int indexOf(Object o) {
    return getKeyValuePairs().indexOf(o);
  }

  /**
   * {@inheritDoc}
   */
  public int lastIndexOf(Object o) {
    return getKeyValuePairs().lastIndexOf(o);
  }

  /**
   * {@inheritDoc}
   */
  public ListIterator<KeyValuePair> listIterator() {
    return getKeyValuePairs().listIterator();
  }

  /**
   * {@inheritDoc}
   */
  public ListIterator<KeyValuePair> listIterator(int index) {
    return getKeyValuePairs().listIterator(index);
  }

  /**
   * {@inheritDoc}
   */
  public KeyValuePair remove(int index) {
    return getKeyValuePairs().remove(index);
  }

  /**
   * {@inheritDoc}
   */
  public KeyValuePair set(int index, KeyValuePair element) {
    return getKeyValuePairs().set(index, element);
  }

  /**
   * {@inheritDoc}
   */
  public List<KeyValuePair> subList(int fromIndex, int toIndex) {
    return getKeyValuePairs().subList(fromIndex, toIndex);
  }
}
