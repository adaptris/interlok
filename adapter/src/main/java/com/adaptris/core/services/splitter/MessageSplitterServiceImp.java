package com.adaptris.core.services.splitter;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Abstract base class for splitting messages based on some criteria.
 * </p>
 */
public abstract class MessageSplitterServiceImp extends ServiceImp {
  public static final String KEY_SPLIT_MESSAGE_COUNT = "splitCount";
  public static final String KEY_CURRENT_SPLIT_MESSAGE_COUNT = "currentSplitCount";
  
  @NotNull
  @Valid
  private MessageSplitter splitter;
  private Boolean ignoreSplitMessageFailures;
  
  /**
   */
  public MessageSplitterServiceImp() {
  }

  /**
   * @see com.adaptris.core.Service
   *      #doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    try (CloseableIterable<AdaptrisMessage> messages = 
         CloseableIterable.FACTORY.ensureCloseable(splitter.splitMessage(msg))) {
      long count = 0;
      for(AdaptrisMessage splitMessage: messages) {
        count++;
          try {
            splitMessage.addMetadata(KEY_CURRENT_SPLIT_MESSAGE_COUNT, Long.toString(count));
            handleSplitMessage(splitMessage);
          }
          catch (ServiceException e) {
            log.debug("Split msg " + splitMessage.getUniqueId() + " failed");
            if (ignoreSplitMessageFailures()) {
              log.debug("IgnoreSplitMessageFailures=true, ignoring failure of " + splitMessage.getUniqueId());
            }
            else {
              throw e;
            }
          }
    	}
      msg.addMetadata(KEY_SPLIT_MESSAGE_COUNT, Long.toString(count));
	} catch (IOException e) {
	  log.warn("Could not close Iterable!", e);
	} catch (CoreException e) {
		throw new ServiceException(e);
	}
  }

  protected abstract void handleSplitMessage(AdaptrisMessage msg)
      throws ServiceException;

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    if (splitter == null) {
      throw new CoreException("Configured Message splitter is null");
    }
  }

  /**
   * <p>
   * Sets the <code>MessageSplitter</code> to use.
   * </p>
   *
   * @param ms the <code>MessageSplitter</code> to use, may not be null
   */
  public void setSplitter(MessageSplitter ms) {
    if (ms == null) {
      throw new IllegalArgumentException("param is null");
    }
    splitter = ms;
  }

  /**
   * <p>
   * Returns the <code>MessageSplitter</code> to use.
   * </p>
   *
   * @return the <code>MessageSplitter</code> to use
   */
  public MessageSplitter getSplitter() {
    return splitter;
  }

  /**
   * @return the ignoreSplitMessageFailures
   */
  public Boolean getIgnoreSplitMessageFailures() {
    return ignoreSplitMessageFailures;
  }

  public boolean ignoreSplitMessageFailures() {
    return getIgnoreSplitMessageFailures() != null ? getIgnoreSplitMessageFailures().booleanValue() : false;
  }
  /**
   * Whether or not to ignore errors on messages that are split.
   *
   * @param b if true, then all split messages will be processed; failures are
   *          simply logged (default false)
   */
  public void setIgnoreSplitMessageFailures(Boolean b) {
    ignoreSplitMessageFailures = b;
  }
}
