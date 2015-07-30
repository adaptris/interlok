package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This RestartStrategy will always return true upon requiresRestart().
 * </p>
 * 
 * @config always-restart-strategy
 */
@XStreamAlias("always-restart-strategy")
public class AlwaysRestartStrategy implements RestartStrategy {

  @Override
  public void messageProcessed(AdaptrisMessage msg) {
  }

  @Override
  public boolean requiresRestart() {
    return true;
  }

}
