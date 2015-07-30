package com.adaptris.util.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of NullConverter that simply returns the value passed in.
 * 
 * @config null-pass-through-converter
 * 
 * @author lchan
 * 
 */
@XStreamAlias("null-pass-through-converter")
public class NullPassThroughConverter implements NullConverter {

  @Override
  public <T> T convert(T t) {
    return t;
  }

}
