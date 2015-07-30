package com.adaptris.util.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of NullConverter that converts null to the empty string ''.
 * 
 * @config null-to-empty-string-converter
 * 
 * @author lchan
 * 
 */
@XStreamAlias("null-to-empty-string-converter")
public class NullToEmptyStringConverter implements NullConverter {

  @Override
  public <T> T convert(T t) {
    T result = t;
    if (t == null) {
      result = (T) "";
    }
    return result;
  }

}
