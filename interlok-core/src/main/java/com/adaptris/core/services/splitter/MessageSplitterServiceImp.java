/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.services.splitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;

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
  @InputFieldDefault(value = "false")
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
    List<Future> tasks = new ArrayList<Future>();
    try (com.adaptris.core.util.CloseableIterable<AdaptrisMessage> messages = com.adaptris.core.util.CloseableIterable
        .ensureCloseable(splitter.splitMessage(msg))) {
      long count = 0;
      for (AdaptrisMessage splitMessage : messages) {
        count++;
        try {
          splitMessage.addMetadata(KEY_CURRENT_SPLIT_MESSAGE_COUNT, Long.toString(count));
          tasks.add(handleSplitMessage(splitMessage));
        } catch (ServiceException e) {
          log.debug("Split msg {} failed", splitMessage.getUniqueId());
          if (ignoreSplitMessageFailures()) {
            log.debug("IgnoreSplitMessageFailures=true, ignoring failure of {}", splitMessage.getUniqueId());
          } else {
            throw e;
          }
        }
      }
      waitForCompletion(tasks);
      msg.addMetadata(KEY_SPLIT_MESSAGE_COUNT, Long.toString(count));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected abstract Future<?> handleSplitMessage(AdaptrisMessage msg)
      throws ServiceException;

  protected void waitForCompletion(List<Future> tasks) throws ServiceException {
    do {
      for (Iterator<Future> i = tasks.iterator(); i.hasNext();) {
        Future f = i.next();
        if (f.isDone()) {
          i.remove();
        }
      }
      if (tasks.size() > 0) {
        LifecycleHelper.waitQuietly(100);
      }
    } while (tasks.size() > 0);
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getSplitter(), "splitter");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {

  }


  /**
   * <p>
   * Sets the <code>MessageSplitter</code> to use.
   * </p>
   *
   * @param ms the <code>MessageSplitter</code> to use, may not be null
   */
  public void setSplitter(MessageSplitter ms) {
    splitter = Args.notNull(ms, "splitter");
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
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreSplitMessageFailures(), false);
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

  // In the current thread, by the time the future returns everything is complete.
  protected static class AlreadyComplete implements Future<Boolean> {

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public Boolean get() {
      return Boolean.TRUE;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) {
      return Boolean.TRUE;
    }

  }
}
