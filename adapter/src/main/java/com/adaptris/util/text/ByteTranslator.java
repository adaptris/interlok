/*
 * $RCSfile: ByteTranslator.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/07/24 11:11:06 $
 * $Author: lchan $
 */
package com.adaptris.util.text;

import java.io.IOException;

/**
 * Abstract Base class for converting strings into bytes and vice versa.
 *
 
 * @author lchan
 *
 */
public abstract class ByteTranslator {
  
  public ByteTranslator() {
  }
  
  /**
   * Translate a string into a byte array.
   *
   * @param s the string.
   * @return the byte array
   * @throws IOException wrapping any underlying exception
   */
  public abstract byte[] translate(String s) throws IOException;

  /**
   * Translate a byte array into a String.
   *
   * @param bytes the byte array.
   * @return the string
   * @throws IOException wrapping any underlying exception
   */
  public abstract String translate(byte[] bytes) throws IOException;
}
