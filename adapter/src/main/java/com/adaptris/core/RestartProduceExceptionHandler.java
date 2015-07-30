package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProduceExceptionHandler} which attempts to restart the {@link Workflow} that had the failure.
 * 
 * @config restart-produce-exception-handler
 */
@XStreamAlias("restart-produce-exception-handler")
public class RestartProduceExceptionHandler extends ProduceExceptionHandlerImp {

  /**
   * @see com.adaptris.core.ProduceExceptionHandler#handle(com.adaptris.core.Workflow)
   */
  public void handle(Workflow workflow) {
    restart(workflow);
  }
}
