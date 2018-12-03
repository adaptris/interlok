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

package com.adaptris.core.lifecycle;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelLifecycleStrategy;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultChannelLifecycleStrategy;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Non Blocking start strategy for channels.
 * <p>
 * This strategy attempts to start each channel in a non-blocking manner through the use of an {@link ExecutorService} for each
 * channel.
 * </p>
 * <p>
 * If this strategy is chosen then it is possible that {@link AdapterLifecycleEvent}s will be generated that do not accurately
 * reflect the internal state of the Adapter.
 * </p>
 * 
 * @config non-blocking-channel-start-strategy
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("non-blocking-channel-start-strategy")
public class NonBlockingChannelStartStrategy extends DefaultChannelLifecycleStrategy {

  private transient Map<String, ExecutorService> channelStarters;

  public NonBlockingChannelStartStrategy() {
    channelStarters = new Hashtable<String, ExecutorService>();
  }

  /**
   * @see ChannelLifecycleStrategy#start(java.util.List)
   */
  @Override
  public void start(List<Channel> channels) throws CoreException {
    handleLifecycle(channels, "Start", e -> { LifecycleHelper.start(e); });
  }

  private void handleLifecycle(List<Channel> channels, final String friendlyText, final ChannelLifecycle oper) {
    for (int i = 0; i < channels.size(); i++) {
      final Channel c = channels.get(i);
      final String name = channelName(c, i);
      if (!c.shouldStart()) {
        continue;
      }
      ExecutorService es = getExecutor(name);
      es.execute(() -> {
        Thread.currentThread().setName(String.format("%s %s", name, friendlyText));
          try {
            oper.initOrStart(c);
          } catch (CoreException e) {
            log.error("Failed to {} channel {}", friendlyText, name, e);
          }
      });
    }
  }

  private ExecutorService getExecutor(String name) {
    ExecutorService es = channelStarters.get(name);
    if (es == null || es.isShutdown()) {
      es = Executors.newSingleThreadExecutor(new ManagedThreadFactory(getClass().getSimpleName()));
      channelStarters.put(name, es);
    }
    return es;
  }


  /**
   * @see ChannelLifecycleStrategy#init(java.util.List)
   */
  @Override
  public void init(List<Channel> channels) throws CoreException {
    handleLifecycle(channels, "Init", e -> { LifecycleHelper.init(e); });
  }

  @Override
  public void stop(List<Channel> channels) {
    stopExecutors(channels);
    super.stop(channels);
  }

  @Override
  public void close(List<Channel> channels) {
    stopExecutors(channels);
    super.close(channels);
  }

  private void stopExecutors(List<Channel> channels) {
    for (int i = 0; i < channels.size(); i++) {
      final Channel c = channels.get(i);
      final String name = channelName(c, i);
      ExecutorService es = getExecutor(name);
      es.shutdownNow();
    }
  }

  private static String channelName(Channel c, int i) {
    return c.hasUniqueId() ? c.getUniqueId() : "Channel(" + i + ")";
  }

  @FunctionalInterface
  private interface ChannelLifecycle {
    public void initOrStart(Channel c) throws CoreException;
  }
}
