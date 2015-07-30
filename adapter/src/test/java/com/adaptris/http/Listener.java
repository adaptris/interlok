/*
 * $Id: Listener.java,v 1.3 2004/09/21 09:56:12 lchan Exp $
 */
package com.adaptris.http;

/** The basic Listener interface.
 */
public interface Listener {

  /** Shutdown this listener
   *  @throws HttpException on error.
   */
  void stop() throws HttpException;

  /** Perform any initialisation prior to be started.  
   *  @throws HttpException on error.
   */
  void initialise() throws HttpException;
  
  /** Start this listener
   *  @throws HttpException on error.
   */
  void start() throws HttpException;
  
  /** Add a request processor to this listener
   *  @param rp a RequestProcessor to be added 
   *  @throws HttpException on error.
   */
  void addRequestProcessor(RequestProcessor rp) throws HttpException;
  
  /** Query if this listener is current alive.
   * 
   * @return true if the listener is alive.
   * @throws HttpException on error.
   */
  boolean isAlive() throws HttpException;

}