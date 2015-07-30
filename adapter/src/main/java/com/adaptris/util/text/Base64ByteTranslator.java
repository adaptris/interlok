/*
 * $RCSfile: Base64ByteTranslator.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/07/24 11:12:55 $
 * $Author: lchan $
 */
package com.adaptris.util.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simply convert to and from base64.
 * 
 * @config base64-byte-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("base64-byte-translator")
public class Base64ByteTranslator extends ByteTranslator {

  public Base64ByteTranslator() {
    super();
  }
  /**
   *
   * @see ByteTranslator#translate(java.lang.String)
   */
  @Override
  public byte[] translate(String s) {
    return Conversion.base64StringToByteArray(s);
  }

  /**
   *
   * @see ByteTranslator#translate(byte[])
   */
  @Override
  public String translate(byte[] bytes) {
    return Conversion.byteArrayToBase64String(bytes);
  }
}
