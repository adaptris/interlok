/*
 * $RCSfile: NullProduceExceptionHandler.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/09/23 00:56:54 $
 * $Author: hfraser $
 */
package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implementation of <code>ProduceExceptionHandler</code> which logs a message only.
 * </p>
 * 
 * @config null-produce-exception-handler
 */
@XStreamAlias("null-produce-exception-handler")
public class NullProduceExceptionHandler extends ProduceExceptionHandlerImp {

  /** @see com.adaptris.core.ProduceExceptionHandler
   *   #handle(com.adaptris.core.Workflow) */
  public void handle(Workflow workflow) {
    log.debug("NullProduceExceptionHandler configured");
  }
}
