package com.adaptris.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * An collection of {@linkplain KeyValuePair} instances.
 * </p>
 * 
 * @config key-value-pair-collection
 */
@XStreamAlias("key-value-pair-collection")
public class KeyValuePairCollection extends KeyValuePairBag {

  @XStreamImplicit(itemFieldName = "key-value-pair")
  private List<KeyValuePair> collection;

  public KeyValuePairCollection() {
    super();
    collection = new ArrayList<KeyValuePair>();
  }

  public KeyValuePairCollection(Collection<KeyValuePair> pairs) {
    this();
    addAll(pairs);
  }

  public KeyValuePairCollection(Properties properties) {
    this();
    addAll(properties);
  }

  public KeyValuePairCollection(Map<String, String> map) {
    this();
    addAll(map);
  }
  @Override
  public Collection<KeyValuePair> getKeyValuePairs() {
    return collection;
  }

  @Override
  public void addKeyValuePair(KeyValuePair pair) {
    if (pair == null) {
      throw new IllegalArgumentException();
    }
    add(pair);
  }
}
