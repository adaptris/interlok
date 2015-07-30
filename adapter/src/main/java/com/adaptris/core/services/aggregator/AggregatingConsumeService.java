package com.adaptris.core.services.aggregator;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.Service;

/**
 * Marker interface for all implementations of {@link AggregatingConsumer} to use.
 * 
 * @author lchan
 * 
 */
public interface AggregatingConsumeService<T extends AdaptrisConnection> extends Service {

}
