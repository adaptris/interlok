package com.adaptris.core.common;

import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;

public interface MultiPayloadDataInputParameter<T> extends DataInputParameter<T>
{
  /**
   * Get the payload ID for extracted data.
   *
   * @return The payload ID.
   */
  String getPayloadId();

  /**
   * Set the payload ID for extracted data.
   *
   * @param id The payload ID.
   */
  void setPayloadId(String id);

  /**
   * {@inheritDoc}.
   */
  T extract(InterlokMessage m) throws InterlokException;

  /**
   * Extract the message payload, for the given ID.
   *
   * @param id The payload ID to extract.
   * @param m The message to extract the payload from.
   *
   * @return The message payload.
   */
  T extract(String id, MultiPayloadAdaptrisMessage m) throws InterlokException;
}
