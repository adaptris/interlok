package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Consumer interface for performing aggregation.
 * 
 * @author lchan
 * 
 */
public interface AggregatingConsumer<T extends AggregatingConsumeService> extends AdaptrisComponent {

  /**
   * Perform the aggregation.
   * 
   * @param msg the current message being processed.
   * @param service the service that relates to this consumer implementation.
   * @throws ServiceException
   */
  void aggregateMessages(AdaptrisMessage msg, T service) throws ServiceException;
}
