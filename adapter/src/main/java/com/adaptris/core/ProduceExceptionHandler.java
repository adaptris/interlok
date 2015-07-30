package com.adaptris.core;


/**
 * <p> 
 * Implementations are pluggable responses to <code>ProduceException</code>s 
 * in a <code>Workflow</code>.
 * </p>
 */
public interface ProduceExceptionHandler {

  /**
   * <p>
   * Handle the <code>ProduceException</code>.
   * </p>
   * @param workflow the <code>Workflow</code> in which the 
   * <code>ProduceException</code> occurred
   */
  void handle(Workflow workflow);
}
