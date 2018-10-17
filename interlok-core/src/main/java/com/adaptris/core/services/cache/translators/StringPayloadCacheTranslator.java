package com.adaptris.core.services.cache.translators;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.cache.CacheValueTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CacheValueTranslator} that retrieves / sets the payload of the message.
 * 
 * @config string-payload-cache-translator
 * 
 * 
 * @author stuellidge
 */
@XStreamAlias("string-payload-cache-translator")
public class StringPayloadCacheTranslator implements CacheValueTranslator<String> {

  private String charEncoding;

  public StringPayloadCacheTranslator() {

  }

  public StringPayloadCacheTranslator(String s) {
    setCharEncoding(s);
  }

  /**
   * @return byte[] containing the payload of the message
   */
  @Override
  public String getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    return msg.getContent();
  }

  /**
   * @param msg the {@link AdaptrisMessage}
   * @param value byte[] to be set as the payload of the message
   * @throws IllegalArgumentException if value is not of type byte[]
   */
  @Override
  public void addValueToMessage(AdaptrisMessage msg, String value) throws CoreException {
    if (isEmpty(getCharEncoding())) {
      msg.setContent(value.toString(), msg.getContentEncoding());
    } else {
      msg.setContent(value.toString(), getCharEncoding());
    }
  }

  public String getCharEncoding() {
    return charEncoding;
  }

  public void setCharEncoding(String ce) {
    charEncoding = ce;
  }


}
