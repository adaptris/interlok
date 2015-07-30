package com.adaptris.core.services;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This RestartStrategy monitors the number of messages processed and if this number passes
 * our maximum threshold, then this strategy will return true upon requiresRestart().  Finally upon 
 * calling requiresRestart, should we return true, we will also reset the internal number of messages
 * processed count back down to zero.
 * </p>
 * <p>
 * The default max-messages-count is set to 50.
 * </p>
 * @author amcgrath
 * 
 * @config message-count-restart-strategy
 *
 */
@XStreamAlias("message-count-restart-strategy")
public class MessageCountRestartStrategy implements RestartStrategy {
  
  private static final int DEFAULT_MAX_MESSAGES_COUNT = 50;
  
  @NotNull
  @AutoPopulated
  private int maxMessagesCount;
  
  private transient int messagesProcessedCount;

  public MessageCountRestartStrategy() {
    this.setMaxMessagesCount(DEFAULT_MAX_MESSAGES_COUNT);
    messagesProcessedCount = 0;
  }
  
  @Override
  public void messageProcessed(AdaptrisMessage msg) {
    messagesProcessedCount ++;
  }

  @Override
  public boolean requiresRestart() {
    boolean result = messagesProcessedCount >= this.getMaxMessagesCount();
    if(result)
      messagesProcessedCount = 0;
    return result;
  }

  public int getMaxMessagesCount() {
    return maxMessagesCount;
  }

  public void setMaxMessagesCount(int maxMessagesCount) {
    this.maxMessagesCount = maxMessagesCount;
  }

}
