package com.adaptris.util.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Simply convert using the default platform encoding.
 * 
 * @config simple-byte-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("simple-byte-translator")
public class SimpleByteTranslator extends ByteTranslator {
  public SimpleByteTranslator() {
    super();
  }

  /**
   *
   * @see ByteTranslator#translate(java.lang.String)
   */
  @Override
  public byte[] translate(String s) {
    return s.getBytes();
  }

  /**
   *
   * @see ByteTranslator#translate(byte[])
   */
  @Override
  public String translate(byte[] bytes) {
    return new String(bytes);
  }

}
