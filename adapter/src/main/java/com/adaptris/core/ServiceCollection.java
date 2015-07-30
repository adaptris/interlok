package com.adaptris.core;

import java.util.List;

/**
 * <p>
 * Defines behaviour common to collections of <code>Service</code>s.
 * This class extends <code>Service</code> and is thus a <code>Service</code>
 * itself.  Implementations may iterate through the collection in order,
 * provide branching, etc.
 * </p>
 */
public interface ServiceCollection extends Service, EventHandlerAware, List<Service> {

  /**
   * <p>
   * Returns a <code>List</code> of the <code>Service</code>s in this
   * collection.
   * </p>
   * @return a <code>List</code> of the <code>Service</code>s in this
   * collection
   */
  List<Service> getServices();

  /**
   * <p>
   * Adds a <code>Service</code> to this collection.
   * </p>
   * @param service the <code>Service</code> to add
   * @throws CoreException wrapping any underlying Exception that may occur
   */
  void addService(Service service) throws CoreException;

  /**
   * <p>
   * Handles any exceptions thrown from an embedded {@linkplain Service}.
   * </p>
   *
   * @param service service which threw the Exception
   * @param e the exception which was thrown
   * @param msg the message which caused the exception
   * @throws ServiceException wrapping the exception if
   *           {@link Service#continueOnFailure()} is false
   */
  void handleException(Service service, AdaptrisMessage msg, Exception e)
    throws ServiceException;
}
