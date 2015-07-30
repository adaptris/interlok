package com.adaptris.core.services.findreplace;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.CharsetByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;
import com.adaptris.util.text.SimpleByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ReplacementSource} implementation which assumes that the value is a byte sequence represented as a sequence of hexadecimal
 * numbers (2 characters per byte).
 * <p>
 * Used with {@link FindAndReplaceService} to replace text in the message; for instance if one of the values from
 * {@link FindAndReplaceService#getFindAndReplaceUnits()} is 0d0a then this will generate an actual replacement value of carriage
 * return linefeed.
 * </p>
 * <p>
 * In most cases you will be using {@link ConfiguredReplacementSource}, however there may be times when the replacement value cannot
 * be described as part of an XML configuration (for instance the 'vertical tab' character, 0x0B, is generally considered invalid
 * depending on the version of the XML standard).
 * </p>
 * 
 * @config hex-sequence-configured-replacement-source
 */
@XStreamAlias("hex-sequence-configured-replacement-source")
public class HexSequenceConfiguredReplacementSource extends AbstractReplacementSource {

  private String charset;

  public HexSequenceConfiguredReplacementSource() {
  }

  public HexSequenceConfiguredReplacementSource(String charset) {
    this(null, charset);
  }

  public HexSequenceConfiguredReplacementSource(String value, String charset) {
    this();
    setCharset(charset);
    setValue(value);
  }

  public String obtainValue(AdaptrisMessage msg) throws ServiceException {
    String result = null;
    ByteTranslator hexToBytes = new HexStringByteTranslator();
    ByteTranslator bytesToString = new SimpleByteTranslator();
    if (!isEmpty(charset)) {
      bytesToString = new CharsetByteTranslator(charset);
    }
    try {
      result = bytesToString.translate(hexToBytes.translate(this.getValue()));
    } catch (Exception e) {
      throw new ServiceException(e);
    }
    return result;
  }

  public String getCharset() {
    return charset;
  }

  /**
   * Set the character set to be used when converting the hexadecimal byte sequence into a string.
   *
   * @param charset the character set, default is 'null' which uses the platform default.
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

}
