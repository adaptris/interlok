package com.adaptris.util.text;

import java.io.IOException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simply converts to and from a Hex String
 * 
 * @config hex-string-byte-translator
 * @author stuellidge
 */
@XStreamAlias("hex-string-byte-translator")
public class HexStringByteTranslator extends ByteTranslator {

  @Override
  public byte[] translate(String s) throws IOException {
    return Conversion.hexStringToByteArray(s);
  }

  @Override
  public String translate(byte[] bytes) throws IOException {
    return Conversion.byteArrayToHexString(bytes);
  }

}
