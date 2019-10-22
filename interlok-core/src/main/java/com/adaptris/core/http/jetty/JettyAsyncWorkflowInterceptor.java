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

import static com.adaptris.core.http.jetty.JettyConstants.JETTY_WRAPPER;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
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
@DisplayOrder(order = {"cacheKey"})
public class JettyAsyncWorkflowInterceptor extends JettyWorkflowInterceptorImpl {

  private static transient Logger log = LoggerFactory.getLogger(JettyAsyncWorkflowInterceptor.class);

  // Should probably use JSR107 for some caching action?? make it pluggable.
  // But for now, same JVM, so a map will do in a pinch
  private static final ExpiringMap<String, JettyWrapper> EXPIRING_CACHE = ExpiringMap.builder()
      .expiration(1L, TimeUnit.HOURS).build();

  public enum Mode {
    /**
     * Indicates that the interceptor is in the workflow that initially received the HTTP Request
     * 
     */
    REQUEST {
      @Override
      void workflowStart(String key, AdaptrisMessage msg) {
        JettyWrapper wrapper = JettyWrapper.unwrap(msg);
        log.trace("Storing {} in cache against {}", wrapper, key);
        EXPIRING_CACHE.put(key, wrapper);
      }

      @Override
      void workflowEnd(AdaptrisMessage orig, AdaptrisMessage result) {
        // Nothing to do.
      }
    },
    /**
     * Indicates that the interceptor is in the workflow that will make the reply to the HTTP Request
     * 
     */
    RESPONSE {
      @Override
      void workflowStart(String key, AdaptrisMessage msg) {
        JettyWrapper wrapper = EXPIRING_CACHE.get(key);
        log.trace("Found {} in cache against {}", wrapper, key);
        if (wrapper != null) {
          EXPIRING_CACHE.remove(key);
          msg.addObjectHeader(JETTY_WRAPPER, wrapper);
        }
      }

      @Override
      void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
        endWorkflow(inputMsg, outputMsg);
      }
    };
    abstract void workflowStart(String key, AdaptrisMessage msg);

    abstract void workflowEnd(AdaptrisMessage input, AdaptrisMessage output);
  }

  @NotNull
  private Mode mode;

  @InputFieldHint(style = "BLANKABLE")
  @InputFieldDefault(value = "")
  @AdvancedConfig(rare = true)
  private String cacheKey;

  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getMode(), "mode");
      if (getMode() == Mode.REQUEST && !StringUtils.isBlank(getCacheKey())) {
        log.warn("Using cache-key in REQUEST mode is dangerous; results may be undefined.");
      }
      super.init();
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }


  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
    getMode().workflowStart(generateKey(inputMsg), inputMsg);
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


  private String generateKey(AdaptrisMessage msg) {
    if (StringUtils.isBlank(getCacheKey())) {
      return msg.getUniqueId();
    }
    // non-existent metadata key returns null
    // %message{XXX} will just return %message{XXX} but there's nothing we can do about that.
    return Args.notBlank(msg.resolve(getCacheKey()), "cacheKey");
  }

  public String getCacheKey() {
    return cacheKey;
  }


  /**
   * Set the key for the cache.
   * <p>
   * Set the key in the cache that will be used to track the underlying response. If left blank, then it will use
   * {@link AdaptrisMessage#getUniqueId()} as the key. If specified, then that key (after metadata solution via
   * {@link AdaptrisMessage#resolve(String)}) will be used.
   * </p>
   * <p>
   * Generally speaking JMS providers will assign a message-id for you when you create a {@code javax.jms.Message}; this becomes the
   * {@link AdaptrisMessage#getUniqueId()} in most cases. If Interlok is in use at all stages, then this can be left blank, as we
   * will try as much to preserve our message unique-id across JMS providers; if there is an external application integrated with
   * JMS, then you might need to specify a value here so that the {@code JMSCorrelationID} (via
   * {@link com.adaptris.core.jms.MetadataCorrelationIdSource}) is used to key the cache when responding back to the HTTP client.
   * </p>
   * <p>
   * Note that because this is an interceptor; the only metadata you have is that which is present <strong>upon entry into the
   * workflow</strong> (i.e. that set by the consumer); in REQUEST mode, you only get the metadata that is set by
   * {@link JettyMessageConsumer} which is not much.
   * </p>
   * 
   * @param s the cache key e.g. %message{JMSCorrelationID} when the mode is {@link Mode#RESPONSE};
   */
  public void setCacheKey(String s) {
    this.cacheKey = s;
  }

  public JettyAsyncWorkflowInterceptor withCacheKey(String s) {
    setCacheKey(s);
    return this;
  }
}
