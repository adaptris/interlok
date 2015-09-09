package com.adaptris.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default strategy for starting channels.
 * <p>
 * This strategy is functionally equivalent to the way in which channel operations were handled in 2.4.x and blocks until all the
 * channel operations are complete.
 * </p>
 * 
 * @config default-channel-lifecycle-strategy
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("default-channel-lifecycle-strategy")
public class DefaultChannelLifecycleStrategy implements ChannelLifecycleStrategy {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private static enum ChannelAction {
    INIT {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.init(c);
      }
    },
    START {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.start(c);
      }
    },
    STOP {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.stop(c);
      }
    },
    CLOSE {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.close(c);
      }
    };
    abstract void invoke(Channel c) throws CoreException;
  }

  public DefaultChannelLifecycleStrategy() {

  }

  /**
   * @see ChannelLifecycleStrategy#start(java.util.List)
   */
  public void start(List<Channel> channels) throws CoreException {
    List<Channel> eligible = new ArrayList<Channel>();
    for (Channel c : channels) {
      if (c.shouldStart()) {
        eligible.add(c);
      }
    }
    action(eligible, ChannelAction.START);
  }

  /**
   * @see ChannelLifecycleStrategy#init(java.util.List)
   */
  public void init(List<Channel> channels) throws CoreException {
    List<Channel> eligible = new ArrayList<Channel>();
    for (Channel c : channels) {
      if (c.shouldStart()) {
        eligible.add(c);
      }
    }
    action(eligible, ChannelAction.INIT);

  }

  /**
   *
   * @see ChannelLifecycleStrategy#stop(java.util.List)
   */
  public void stop(List<Channel> channels) {
    actionQuietly(channels, ChannelAction.STOP);
  }

  /**
   *
   * @see ChannelLifecycleStrategy#close(java.util.List)
   */
  public void close(List<Channel> channels) {
    actionQuietly(channels, ChannelAction.CLOSE);
  }

  private void action(List<Channel> channels, ChannelAction op) throws CoreException {
    String originalName = Thread.currentThread().getName();
    try {
      for (int i = 0; i < channels.size(); i++) {
        Channel c = channels.get(i);
        String threadName = originalName + ":" + (c.hasUniqueId() ? c.getUniqueId() : "Channel(" + i + ")") + "." + op.name();
        Thread.currentThread().setName(threadName);
        op.invoke(c);
      }
    }
    finally {
      Thread.currentThread().setName(originalName);
    }
  }

  private void actionQuietly(List<Channel> channels, ChannelAction op) {
    try {
      action(channels, op);
    }
    catch (CoreException ignored) {
    }
  }
}
