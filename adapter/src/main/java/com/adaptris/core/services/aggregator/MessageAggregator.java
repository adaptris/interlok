package com.adaptris.core.services.aggregator;

import java.util.Collection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.SplitJoinService;

/**
 * Interface for creating a single {@link AdaptrisMessage} instance from multiple Messages.
 * 
 * @see SplitJoinService
 */
public interface MessageAggregator {

  /**
   * <p>
   * Joins multiple {@link AdaptrisMessage}s into a single AdaptrisMessage objects. Preservation of metadata is down to the
   * implementation.
   * </p>
   * 
   * @param msg the msg to insert all the messages into
   * @param msgs the list of messages to join.
   * @throws CoreException wrapping any other exception
   */
  void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException;

}
