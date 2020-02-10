package com.adaptris.core.services.cache.translators;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.cache.CacheKeyTranslator;
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
public class StringPayloadCacheTranslator implements CacheValueTranslator<String>, CacheKeyTranslator {

  private String charEncoding;

  public StringPayloadCacheTranslator() {

  }

  public StringPayloadCacheTranslator(String s) {
    setCharEncoding(s);
  }

  @Override
  public String getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    return msg.getContent();
  }

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

  @Override
  public String getKeyFromMessage(AdaptrisMessage msg) throws CoreException {
    return getValueFromMessage(msg);
  }

}
