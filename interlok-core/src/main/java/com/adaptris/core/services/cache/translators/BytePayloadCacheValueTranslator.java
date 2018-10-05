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

  /**
   * @return byte[] containing the payload of the message
   */
  @Override
  public byte[] getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    return msg.getPayload();
  }

  /**
   * @param msg the {@link AdaptrisMessage}
   * @param value byte[] to be set as the payload of the message
   * @throws IllegalArgumentException if value is not of type byte[]
   */
  @Override
  public void addValueToMessage(AdaptrisMessage msg, byte[] value) throws CoreException {
    msg.setPayload(value);
  }

}
