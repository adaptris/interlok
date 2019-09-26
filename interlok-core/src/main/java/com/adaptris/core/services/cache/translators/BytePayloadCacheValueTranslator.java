package com.adaptris.core.services.cache.translators;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.cache.CacheValueTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CacheValueTranslator} that retrieves / sets the payload of the message.
 * 
 * @config byte-payload-cache-value-translator
 * 
 * @author stuellidge
 */
@XStreamAlias("byte-payload-cache-value-translator")
public class BytePayloadCacheValueTranslator implements CacheValueTranslator<byte[]> {

  @Override
  public byte[] getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    return msg.getPayload();
  }

  @Override
  public void addValueToMessage(AdaptrisMessage msg, byte[] value) throws CoreException {
    msg.setPayload(value);
  }

}
