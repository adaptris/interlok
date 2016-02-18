package com.adaptris.core.metadata;

import com.adaptris.core.AdaptrisMessage;

public class MetadataResolver {

  private static final String REFERENCE_PREFIX = "$$";
  
  public static String resolveKey(AdaptrisMessage message, String key) {
    if(key == null)
      return null;
    if(key.startsWith(REFERENCE_PREFIX)) {
      return message.getMetadataValue(key.substring(REFERENCE_PREFIX.length()));
    } else
      return key;
  }
  
}
