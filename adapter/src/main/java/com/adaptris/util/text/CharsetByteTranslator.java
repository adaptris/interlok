package com.adaptris.util.text;

import java.io.UnsupportedEncodingException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simply convert bytes into strings using the configured encoding.
 * 
 * @config charset-byte-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("charset-byte-translator")
public class CharsetByteTranslator extends ByteTranslator {

  private String charsetEncoding;

  /**
   * Default charset encoding is UTF-8.
   *
   */
  public CharsetByteTranslator() {
    this("UTF-8");
  }

  public CharsetByteTranslator(String charset) {
    super();
    setCharsetEncoding(charset);
  }
  
  /**
   *
   * @see ByteTranslator#translate(java.lang.String)
   */
  @Override
  public byte[] translate(String s) throws UnsupportedEncodingException {
    return s.getBytes(charsetEncoding);
  }

  /**
   *
   * @see ByteTranslator#translate(byte[])
   */
  @Override
  public String translate(byte[] bytes) throws UnsupportedEncodingException {
    return new String(bytes, charsetEncoding);
  }

  /**
   * @return the charsetEncoding
   */
  public String getCharsetEncoding() {
    return charsetEncoding;
  }

  /**
   * @param c the charsetEncoding to set
   */
  public void setCharsetEncoding(String c) {
    this.charsetEncoding = c;
  }

}
