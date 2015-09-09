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
