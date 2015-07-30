package com.adaptris.core;

import java.util.Collection;

/**
 * Strategy for handling connection lifecycle within a {@link SharedComponentList}.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface SharedComponentLifecycleStrategy {


    /**
   * Initialise a list of channels.
   * 
   * @param conns a list of channels that require initialising.
   * @throws CoreException wrapping any underlying exception.
   */
  void init(Collection<AdaptrisConnection> conns) throws CoreException;
  
  /**
   * Start a list of connections.
   * 
   * @param conns a list of connections that have already been initialised.
   * @throws CoreException wrapping any underlying exception.
   */
  void start(Collection<AdaptrisConnection> conns) throws CoreException;

  /**
   * Stop a list of connections.
   * 
   * @param conns a list of channels that have previously been started.
   */
  void stop(Collection<AdaptrisConnection> conns);

  /**
   * Close a list of connections.
   * 
   * @param conns a list of connections.
   */
  void close(Collection<AdaptrisConnection> conns);
}
