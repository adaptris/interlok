package com.adaptris.core.interceptor;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.validation.Valid;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.ParentRuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponent;
import com.adaptris.core.runtime.RuntimeInfoComponentFactory;
import com.adaptris.core.runtime.WorkflowManager;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Interceptor that emits a {@link Notification} if a message has exceeded the specified threshold
 * for processing within a workflow.
 * 
 * <p>
 * The {@link Notification#setUserData(Object)} part of the notification is a {@link Properties}
 * object containing information about the message that exceeded the interceptors threshold. Note
 * that notifications are emitted whenever a message is deemed to have exceeded the threshold; you
 * might get multiple notifications if a message exceeds the threshold, fails and is automatically
 * retried (which again exceeds the threshold).
 * </p>
 * <p>
 * A cleanup thread is also started as part of this interceptor which purges internal data on a
 * periodic basis; this period is calculated based on the notification threshold (+1 minute) and any
 * messages that are outstanding will have a notification emitted for it, and removed. Because of
 * this, it will be possible to be notified before the message formally exits the workflow, but the
 * notification will still be after the threshold has been exceeded. If this occurs, then the
 * endTime and timeTaken markers are set to {@code -1}
 * </p>
 * 
 * @author lchan
 * @license STANDARD
 * @since 3.0.4
 */
@XStreamAlias("slow-message-notification")
public class SlowMessageNotification extends NotifyingInterceptor {

  private static final String MESSAGE_PREFIX = "Threshold exceeded for ";

  /**
   * Key within the properties containing the messageID
   */
  public static final String KEY_MESSAGE_ID = "message.id";
  /**
   * Key within the properties containing the start time in milliseconds
   * 
   */
  public static final String KEY_MESSAGE_START = "message.startTime";
  /**
   * Key within the properties containing the end time in milliseconds
   * 
   */
  public static final String KEY_MESSAGE_END = "message.endTime";
  /**
   * Key within the properties containing the time taken in milliseconds
   * 
   */
  public static final String KEY_MESSAGE_DURATION = "message.timeTaken";
  /**
   * Key within the properties containing whether the message was successful or not
   * 
   */
  public static final String KEY_MESSAGE_SUCCESS = "message.wasSuccessful";

  private static final TimeInterval DEFAULT_THRESHOLD = new TimeInterval(1L, TimeUnit.MINUTES);

  @Valid
  private TimeInterval notifyThreshold;
  private transient Map<String, MessageThroughputStat> currentMessages;
  
  private transient ScheduledExecutorService executor;
  private transient ScheduledFuture cleanupTask;
  private transient TimeInterval cleanupInterval;

  static {
    RuntimeInfoComponentFactory.registerComponentFactory(new JmxFactory());
  }


  public SlowMessageNotification() {
    currentMessages = new ConcurrentHashMap<>();
  }

  public SlowMessageNotification(String uid, TimeInterval threshold) {
    this();
    setUniqueId(uid);
    setNotifyThreshold(threshold);
  }

  SlowMessageNotification(String uid, TimeInterval threshold, TimeInterval cleanup) {
    this(uid, threshold);
    cleanupInterval = cleanup;
  }


  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
    currentMessages.put(inputMsg.getUniqueId(), new MessageThroughputStat(inputMsg.getUniqueId()));
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    String msgId = inputMsg.getUniqueId();
    MessageThroughputStat c = currentMessages.get(msgId);
    if (c != null) {
      currentMessages.remove(msgId);
      c.updateStats(inputMsg, outputMsg);
      if (c.timeTaken > notifyThreshold()) {
        sendNotification(c);
      }
    }
    return;
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
    executor = Executors.newSingleThreadScheduledExecutor(new ManagedThreadFactory());
    scheduleTask();
  }

  @Override
  public void stop() {
    cancelTask();
    shutdownExecutor();
  }

  @Override
  public void close() {
    currentMessages.clear();
  }

  public TimeInterval getNotifyThreshold() {
    return notifyThreshold;
  }

  /**
   * Specify the duration which if exceeded a {@link Notification} will be sent.
   * 
   * @param t the duration, if not specified then 1 minute.
   */
  public void setNotifyThreshold(TimeInterval t) {
    this.notifyThreshold = t;
  }

  long notifyThreshold() {
    return getNotifyThreshold() != null ? getNotifyThreshold().toMilliseconds() : DEFAULT_THRESHOLD
        .toMilliseconds();
  }

  long cleanupInterval() {
    return cleanupInterval != null ? cleanupInterval.toMilliseconds() : notifyThreshold()
        + DEFAULT_THRESHOLD.toMilliseconds();
  }

  private void scheduleTask() {
    cleanupTask =
        executor.scheduleWithFixedDelay(new CleanupTask(), 100L, cleanupInterval(),
            TimeUnit.MILLISECONDS);
    log.trace("Scheduled {}", cleanupTask);
  }


  private void cancelTask() {
    if (cleanupTask != null) {
      cleanupTask.cancel(true);
      log.trace("Poller {} cancelled", cleanupTask);
      cleanupTask = null;
    }
  }

  private void shutdownExecutor() {
    if (executor != null) {
      executor.shutdown();
      boolean success = false;
      try {
        success =
            executor.awaitTermination(DEFAULT_THRESHOLD.toMilliseconds(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
      }
      if (!success) {
        log.trace("Pool failed to shutdown in {}ms, forcing shutdown",
            DEFAULT_THRESHOLD.toMilliseconds());
        executor.shutdownNow();
      }
      executor = null;
    }
  }

  private void sendNotification(MessageThroughputStat d) {
    sendNotification(MESSAGE_PREFIX + d.messageId, d.asProperties());
  }

  private class MessageThroughputStat {

    private long startTime;
    private long timeTaken = -1;
    private long endTime = -1;
    private String messageId;
    private boolean wasSuccessful;

    MessageThroughputStat(String msgId) {
      startTime = System.currentTimeMillis();
      messageId = msgId;
    }

    void updateStats(AdaptrisMessage... msgs) {
      endTime = System.currentTimeMillis();
      timeTaken = Math.abs(endTime - startTime);
      wasSuccessful = wasSuccessful(msgs);
    }

    Properties asProperties() {
      Properties p = new Properties();
      p.setProperty(KEY_MESSAGE_DURATION, String.valueOf(timeTaken));
      p.setProperty(KEY_MESSAGE_START, String.valueOf(startTime));
      p.setProperty(KEY_MESSAGE_ID, messageId);
      p.setProperty(KEY_MESSAGE_END, String.valueOf(endTime));
      p.setProperty(KEY_MESSAGE_SUCCESS, String.valueOf(wasSuccessful));
      return p;
    }
  }


  private class CleanupTask implements Runnable {

    /** @see java.lang.Runnable#run() */
    @Override
    public void run() {
      long now = System.currentTimeMillis();
      List<String> keys = new ArrayList<>(currentMessages.keySet());
      for (String key : keys) {
        MessageThroughputStat c = currentMessages.get(key);
        if (c != null) {
          if (Math.abs(now - c.startTime) > notifyThreshold()) {
            // the threshold has been exceeded even though we haven't notified on the message.
            // Let's just notify on it, and discard it.
            sendNotification(c);
            currentMessages.remove(key);
          }
        }
      }
    }
  }


  private static class JmxFactory extends RuntimeInfoComponentFactory {

    @Override
    protected boolean isSupported(AdaptrisComponent e) {
      if (e != null && e instanceof SlowMessageNotification) {
        return !isEmpty(((SlowMessageNotification) e).getUniqueId());
      }
      return false;
    }

    @Override
    protected RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent,
        AdaptrisComponent e) throws MalformedObjectNameException {
      return new InterceptorNotification((WorkflowManager) parent, (SlowMessageNotification) e);
    }

  }

}
