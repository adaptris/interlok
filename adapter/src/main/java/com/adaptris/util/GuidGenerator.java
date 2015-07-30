package com.adaptris.util;

import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Creates a GUID using {@link UUID#randomUUID()}.
 * 
 * 
 @config guid-generator
 */
@XStreamAlias("guid-generator")
public class GuidGenerator implements IdGenerator {

  public GuidGenerator() {
  }

  /**
   * Get the next unique ID.
   *
   * @return the next unique ID
   */
  public String getUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   *
   * @see com.adaptris.util.IdGenerator#create(java.lang.Object)
   */
  @Override
  public String create(Object msg) {
    return getUUID();
  }

  /**
   * Get a safe UUID
   *
   * @return a UUID stripped of colons and dashes
   */
  public String safeUUID() {
    return getUUID().replaceAll(":", "").replaceAll("-", "");
  }
}
