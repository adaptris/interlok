package com.adaptris.core.services.metadata;

import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.CharsetByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class will encode a metadata value into its equivalent hex hex encoded metadata value(s) using the specified character
 * encoding
 * 
 * @config string-to-hex-metadata-service
 * @license BASIC
 * @see HexToStringService
 * 
 */
@XStreamAlias("string-to-hex-metadata-service")
public class StringToHexService extends HexToStringService {

  public StringToHexService() {
    super();
  }

  public StringToHexService(String regex) {
    super(regex);
    setCharset(UTF_8);
  }

  @Override
  protected String reformat(String s, String msgCharset) throws Exception {
    ByteTranslator bytesToHex = new HexStringByteTranslator();
    ByteTranslator stringToBytes = new CharsetByteTranslator(getCharacterEncoding());
    return bytesToHex.translate(stringToBytes.translate(s));
  }

}
