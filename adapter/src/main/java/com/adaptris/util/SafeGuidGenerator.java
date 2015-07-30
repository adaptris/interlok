package com.adaptris.util;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Overrides standard {@link GuidGenerator} behaviour using {@link GuidGenerator#safeUUID()} for {@link IdGenerator#create(Object)}
 * instead.
 * 
 * 
 * @config safe-guid-generator
 */
@XStreamAlias("safe-guid-generator")
public class SafeGuidGenerator extends GuidGenerator {

  public SafeGuidGenerator() {
  }

  /**
   * 
   * @see com.adaptris.util.IdGenerator#create(java.lang.Object)
   */
  @Override
  public String create(Object msg) {
    return safeUUID();
  }
}
