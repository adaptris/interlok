package com.adaptris.interlok.config;

/**
 * <p>
 * DataDestinations are a generic configuration item, that allow you to specify where data from needed by a service
 * will come from or be saved to.
 * </p>
 * <p>
 * Please check the service documentation to make sure that service supports DataDestinations.
 * </p>
 * @author Aaron
 *
 */
public interface DataDestination<S, T> extends DataInputParameter<S>, DataOutputParameter<T> {
  
}
