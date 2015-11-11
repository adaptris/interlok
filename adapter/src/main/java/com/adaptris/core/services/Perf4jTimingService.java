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

package com.adaptris.core.services;

import static com.adaptris.core.util.LoggingHelper.friendlyName;
import static org.apache.commons.lang.StringUtils.isBlank;

import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform a Perf4j StopWatch timing around an arbitary service using Log4JStopWatch.
 * <p>
 * Within the adapter, only producers and workflows have Perf4J performance statistics profiling
 * enabled; you may wish to monitor the performance of various services. If this is the case, then
 * you can wrap the service using {@link Perf4jTimingService} with a custom tag.
 * </p>
 * 
 * @config perf4j-timing-service
 * 
 * 
 * @see Log4JStopWatch
 */
@XStreamAlias("perf4j-timing-service")
public class Perf4jTimingService extends ServiceImp implements EventHandlerAware {

  @NotNull
  @AutoPopulated
  private Service service;
  private String tag;
  private Boolean includeLifecycleStats;
  private String logCategory;
  private transient EventHandler eventHandler;

  private enum Lifecycle {
    init {
      @Override
      void control(Service s) {
        try {
          LifecycleHelper.init(s);
        }
        catch (CoreException e) {
          throw new LifecycleException(e);
        }
      }
    },
    start {
      @Override
      void control(Service s) {
        try {
          LifecycleHelper.start(s);
        }
        catch (CoreException e) {
          throw new LifecycleException(e);
        }
      }
    },
    stop {
      @Override
      void control(Service s) {
        LifecycleHelper.stop(s);
      }
    },
    close {
      @Override
      void control(Service s) {
        LifecycleHelper.close(s);
      }
    };
    abstract void control(Service s);
  }

  public Perf4jTimingService() {
    setService(new NullService());
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    this.eventHandler = eh;
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    StopWatch stopWatch = createTimer(createTag());
    try {
      service.doService(msg);
    }
    finally {
      stopWatch.stop();
    }
  }

  private void handleLifecycle(Lifecycle c) {
    if (includeLifecycleStats()) {
      StopWatch stopWatch = createTimer(createTag() + "." + c.name());
      c.control(getService());
      stopWatch.stop();
    }
    else {
      c.control(getService());
    }
  }

  private boolean includeLifecycleStats() {
    return includeLifecycleStats == null ? false : includeLifecycleStats.booleanValue();
  }

  private String createTag() {
    return isBlank(getTag()) ? friendlyName(service) : getTag();
  }

  private StopWatch createTimer(String tag) {
    return isBlank(getLogCategory()) ? new Log4JStopWatch(tag) : new Log4JStopWatch(tag, Logger.getLogger(getLogCategory()));
  }

  @Override
  protected void initService() throws CoreException {
    try {
      LifecycleHelper.registerEventHandler(service, eventHandler);
      handleLifecycle(Lifecycle.init);
    } catch (LifecycleException e) {
      throw (CoreException) e.getCause();
    }
  }

  @Override
  protected void closeService() {
    handleLifecycle(Lifecycle.close);
  }

  @Override
  public void start() throws CoreException {
    try {
      handleLifecycle(Lifecycle.start);
    }
    catch (LifecycleException e) {
      throw (CoreException) e.getCause();
    }
  }

  @Override
  public void stop() {
    handleLifecycle(Lifecycle.stop);
  }

  public Service getService() {
    return service;
  }

  @Override
  public void prepare() throws CoreException {
    getService().prepare();
  }


  /**
   * Set the service that will have it's performance tracked.
   *
   * @param wrappedService the service.
   */
  public void setService(Service wrappedService) {
    service = Args.notNull(wrappedService, "service");
  }

  public String getTag() {
    return tag;
  }

  /**
   * Set the tag to be used for the performance statistics.
   *
   * <p>
   * If you additional specify lifeCyclePerformance to be true, then the tag
   * will be used as a prefix to the additional tags '.init', '.start', '.stop',
   * '.close'
   * </p>
   *
   * @param tag the tag, default is null which resolves to the wrapped services
   *          name.
   * @see #setIncludeLifecycleStats(Boolean)
   */
  public void setTag(String tag) {
    this.tag = tag;
  }

  public Boolean getIncludeLifecycleStats() {
    return includeLifecycleStats;
  }

  /**
   * Set this to true if you wish to track the performance of the individual
   * {@link AdaptrisComponent} lifecycle methods.
   * <p>
   * Setting this to true will generate the additional tags "{@link #getTag()}
   * .init", "{@link #getTag()}.start", "{@link #getTag()}.stop", "
   * {@link #getTag()} .close" matching the lifecycle of the wrapped service
   * copmonent.
   * </p>
   *
   * @param b default is false, true to enable.
   */
  public void setIncludeLifecycleStats(Boolean b) {
    includeLifecycleStats = b;
  }

  public String getLogCategory() {
    return logCategory;
  }

  /**
   * Set the logging category for the performance logger.
   *
   * @param logCategory the logcategory, default null which uses the default
   *          (org.perf4j.TimingLogger)
   */
  public void setLogCategory(String logCategory) {
    this.logCategory = logCategory;
  }

  private static final class LifecycleException extends RuntimeException {
    private static final long serialVersionUID = 2010102101L;

    public LifecycleException(Throwable cause) {
      super(cause);
    }
  }
}
