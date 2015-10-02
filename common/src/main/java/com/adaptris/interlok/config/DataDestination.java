package com.adaptris.interlok.config;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

/**
 * <p>
 * DataDestinations are a generic configuration item, that allow you to specify where data from needed by a service
 * will come from or be saved to.
 * </p>
 * <p>
 * Please check the service documentation to make sure that service supports DataDestinations.
 * </p>
 * <p>
 * A typical use might be for something like the {@link XPathService}, which allows you to use DataDestinations
 * to specify the location of the source xml, xpath expression and the target destination for any results returned.
 * </p>
 * @author Aaron
 *
 */
public interface DataDestination<T> {
  
  public T getData(InterlokMessage message) throws InterlokException;
  
  public void setData(InterlokMessage message, T data) throws InterlokException;

}
