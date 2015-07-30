package com.adaptris.core;


/**
 * Abstract implementation of {@link LogHandler} that provides nominal lifecycle and license handling.
 * 
 * @author lchan
 *
 */
public abstract class LogHandlerImp implements LogHandler {

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

}
