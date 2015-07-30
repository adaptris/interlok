package com.adaptris.util.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of NullConverter that throws an UnsupportedOperationException if null is the param.
 * 
 * @config nulls-not-supported-converter
 * 
 * @author lchan
 * 
 */
@XStreamAlias("nulls-not-supported-converter")
public class NullsNotSupportedConverter implements NullConverter {

  @Override
  public <T> T convert(T t) {
    if (t == null) {
      throw new UnsupportedOperationException("Null values are not supported");
    }
    return t;
  }

}
