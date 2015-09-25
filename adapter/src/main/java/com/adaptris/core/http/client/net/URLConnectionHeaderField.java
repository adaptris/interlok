package com.adaptris.core.http.client.net;

import java.io.Serializable;
import java.util.List;

/**
 * Wrapper class around the {@link java.net.HttpURLConnection#getHeaderFields()} for insertion into object metadata.
 * 
 * @author lchan
 *
 */
public final class URLConnectionHeaderField implements Serializable {

  private static final long serialVersionUID = 2015092501L;
  private String key;
  private List<String> values;

  public URLConnectionHeaderField() {

  }

  public URLConnectionHeaderField(String key, List<String> values) {
    this();
    setKey(key);
    setValues(values);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

}
