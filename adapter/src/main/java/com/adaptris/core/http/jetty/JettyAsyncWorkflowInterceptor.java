/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.http.jetty;

import static com.adaptris.core.CoreConstants.JETTY_RESPONSE_KEY;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.jodah.expiringmap.ExpiringMap;

/**
 * Allows you to handle a single HTTP request across 2 workflows within the same Interlok instance.
 * 
 * <p>
 * Configure one of these with {@code mode=REQUEST} in the workflow with the {@link JettyMessageConsumer}; and then in your reply
 * workflow, configure one of these with {@code mode=RESPONSE}. When the reply workflow is finished; then the jetty response will be
 * committed back to the caller.
 * </p>
 * 
 * @config jetty-async-workflow-interceptor
 * @since 3.7.3
 *
 */
@XStreamAlias("jetty-async-workflow-interceptor")
@AdapterComponent
@ComponentProfile(summary = "Interceptor that allows a HTTP request to be handled multiple workflows", tag = "interceptor,http,https", since = "3.7.3")
public class JettyAsyncWorkflowInterceptor extends JettyWorkflowInterceptorImpl {

  private static transient Logger log = LoggerFactory.getLogger(JettyAsyncWorkflowInterceptor.class);

  // Should probably use JSR107 for some caching action?? make it pluggable.
  // But for now, same JVM, so a map will do in a pinch
  private static final ExpiringMap<String, CacheEntryWrapper> EXPIRING_CACHE = ExpiringMap.builder().expiration(1L, TimeUnit.HOURS).build();

  public enum Mode {
    REQUEST {
      @Override
      void workflowStart(AdaptrisMessage msg) {
        CacheEntryWrapper wrapper = new CacheEntryWrapper();
        wrapper.monitor = (JettyConsumerMonitor) msg.getObjectHeaders().get(MESSAGE_MONITOR);
        wrapper.response = (HttpServletResponse) msg.getObjectHeaders().get(JETTY_RESPONSE_KEY);
        log.trace("Storing {} in cache against {}", wrapper, msg.getUniqueId());
        EXPIRING_CACHE.put(msg.getUniqueId(), wrapper);
      }

      @Override
      void workflowEnd(AdaptrisMessage orig, AdaptrisMessage result) {
        // Nothing to do.
      }
    },
    RESPONSE {
      @Override
      void workflowStart(AdaptrisMessage msg) {
        CacheEntryWrapper wrapper = EXPIRING_CACHE.get(msg.getUniqueId());
        log.trace("Found {} in cache against {}", wrapper, msg.getUniqueId());
        if (wrapper != null) {
          EXPIRING_CACHE.remove(msg.getUniqueId());
          msg.addObjectHeader(MESSAGE_MONITOR, wrapper.monitor);
          msg.addObjectHeader(JETTY_RESPONSE_KEY, wrapper.response);
        }
      }

      @Override
      void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
        endWorkflow(inputMsg, outputMsg);
      }
    };
    abstract void workflowStart(AdaptrisMessage msg);

    abstract void workflowEnd(AdaptrisMessage input, AdaptrisMessage output);
  }

  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getMode(), "mode");
      super.init();
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @NotNull
  private Mode mode;

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
    getMode().workflowStart(inputMsg);
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    getMode().workflowEnd(inputMsg, outputMsg);
  }

  public Mode getMode() {
    return mode;
  }

  /**
   * Set the mode.
   * 
   * @param m
   */
  public void setMode(Mode m) {
    this.mode = Args.notNull(m, "mode");
  }

  public JettyAsyncWorkflowInterceptor withMode(JettyAsyncWorkflowInterceptor.Mode m) {
    setMode(m);
    return this;
  }

  public static boolean cacheContains(String msgId) {
    return EXPIRING_CACHE.containsKey(msgId);
  }

  public static boolean removeEntry(String msgId) {
    return EXPIRING_CACHE.remove(msgId) != null;
  }

  private static class CacheEntryWrapper {
    private JettyConsumerMonitor monitor;
    private HttpServletResponse response;
  }

}
