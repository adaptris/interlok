package com.adaptris.core.services.metadata;

import java.io.ByteArrayOutputStream;

import com.adaptris.core.AdaptrisMessage;

/**
 * Enumeration of where the two types of metadata.
 *
 */
public enum MetadataTarget {
  /**
   * Standard Metadata.
   *
   */
  Standard {
    @Override
    public void apply(AdaptrisMessage msg, String key, ByteArrayOutputStream value) {
      msg.addMetadata(key, value.toString());
    }
  },
  /**
   * Object Metadata.
   *
   */
  Object {
    @Override
    public void apply(AdaptrisMessage msg, String key, ByteArrayOutputStream value) {
      msg.addObjectHeader(key, value.toByteArray());
    }
  };

  public abstract void apply(AdaptrisMessage msg, String key, ByteArrayOutputStream value);

}
