package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;

/**
 * Generate a {@link ConsumeDestination} based on the message currently being processed.
 * 
 * 
 */
public interface ConsumeDestinationGenerator {

  ConsumeDestination generate(AdaptrisMessage msg);
}