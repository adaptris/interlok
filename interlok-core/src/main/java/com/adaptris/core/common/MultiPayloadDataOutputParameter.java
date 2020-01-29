package com.adaptris.core.common;

import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;

public interface MultiPayloadDataOutputParameter<T> extends DataOutputParameter<T>
{
  /**
   * Get the payload ID for inserted data.
   *
   * @return The payload ID.
   */
  String getPayloadId();

  /**
   * Set the payload ID for inserted data.
   *
   * @param id The payload ID.
   */
  void setPayloadId(String id);

  /**
   * {@inheritDoc}.
   */
  void insert(T data, InterlokMessage m) throws InterlokException;

  /**
   * Insert the data into the message, with the given ID.
   *
   * @param data The data to insert.
   * @param id The payload ID.
   * @param m The multi-payload message.
   */
  void insert(T data, String id, MultiPayloadAdaptrisMessage m) throws InterlokException;
}
