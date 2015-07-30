package com.adaptris.util.text;


/**
 * Interface for handling null parameters.
 *
 * @author lchan
 * 
 */
public interface NullConverter {

  /**
   * Convert a null into something more meaningful.
   *
   */
  <T> T convert(T t);
}
