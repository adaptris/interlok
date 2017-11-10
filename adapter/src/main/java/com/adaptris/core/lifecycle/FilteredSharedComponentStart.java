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

import static com.adaptris.core.util.LoggingHelper.friendlyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.SharedComponentLifecycleStrategy;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Filtered start strategy for {@link com.adaptris.core.SharedComponentList}
 * <p>
 * In a standard Adapter lifecycle; all shared connections are started before any channels are started. If a connection is
 * referenced by a Channel that is not set to auto-start; then this can cause a connection to be made which would waste resources.
 * Use this strategy to only start specific named connections. In any event, if a connection is not started when a channel or
 * service that requires it is started, it will be started.
 * </p>
 * <p>
 * All the includes are processed first to generate a subset of connections that is then used to process any exclusions. The
 * <code>include-connection-id</code> and <code>exclude-connection-id</code> should be a String that will be treated as a regular
 * expression format. If you do not specify any includes, then this implicitly means all elements are included. If you do not
 * specify any excludes then nothing is excluded from the inclusion subset.
 * </p>
 * <p>
 * Typically, you shouldn't need both includes and excludes because your skill with regular expressions mean that you can handle
 * everything you need in either block. It is processed this way for completeness.
 * </p>
 * 
 * @config filtered-shared-component-start
 * @author lchan
 */
@XStreamAlias("filtered-shared-component-start")
public class FilteredSharedComponentStart implements SharedComponentLifecycleStrategy {

  private static enum ConnectionAction {
    INIT {
      @Override
      void invoke(AdaptrisConnection c) throws CoreException {
        LifecycleHelper.init(c);
      }
    },
    START {
      @Override
      void invoke(AdaptrisConnection c) throws CoreException {
        LifecycleHelper.start(c);
      }
    };
    abstract void invoke(AdaptrisConnection c) throws CoreException;
  }

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private transient Map<String, ExecutorService> connectionStarters;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean threadedStart;
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "include")
  private List<String> includes;
  @NotNull
  @AutoPopulated
  @XStreamImplicit(itemFieldName = "exclude")
  private List<String> excludes;

  private transient List<Pattern> includePatterns;
  private transient List<Pattern> excludePatterns;

  public FilteredSharedComponentStart() {
    connectionStarters = new HashMap<>();
    setIncludes(new ArrayList<String>());
    setExcludes(new ArrayList<String>());
    includePatterns = new ArrayList<Pattern>();
    excludePatterns = new ArrayList<Pattern>();
  }

  public FilteredSharedComponentStart(Boolean threaded) {
    this();
    setThreadedStart(threaded);
  }

  @Override
  public void init(Collection<AdaptrisConnection> conns) throws CoreException {
    Collection<AdaptrisConnection> actual = filter(new ArrayList<AdaptrisConnection>(conns));
    logActivity(ConnectionAction.INIT, actual);
    for (AdaptrisConnection c : actual) {
      execute(ConnectionAction.INIT, c);
    }
  }

  @Override
  public void start(Collection<AdaptrisConnection> conns) throws CoreException {
    Collection<AdaptrisConnection> actual = filter(new ArrayList<AdaptrisConnection>(conns));
    logActivity(ConnectionAction.START, actual);
    for (AdaptrisConnection c : actual) {
      execute(ConnectionAction.START, c);
    }
  }

  @Override
  public void stop(Collection<AdaptrisConnection> conns) {
    shutdownExecutors();
    for (AdaptrisConnection c : conns) {
      LifecycleHelper.stop(c);
    }
  }

  @Override
  public void close(Collection<AdaptrisConnection> conns) {
    shutdownExecutors();
    for (AdaptrisConnection c : conns) {
      LifecycleHelper.close(c);
    }
  }

  private void execute(final ConnectionAction action, final AdaptrisConnection comp) throws CoreException {
    if (threadedStart()) {
      final String name = comp.getUniqueId();
      ExecutorService myExecutor = getExecutor(comp.getUniqueId());
      final String threadName = this.getClass().getSimpleName();
      myExecutor.execute(new Runnable() {
        @Override
        public void run() {
          Thread.currentThread().setName(threadName);
          try {
            action.invoke(comp);
          }
          catch (Exception e) {
            log.error("Failed to {} connection {}", action, friendlyName(comp), e);
          }
        }
      });
    }
    else {
      action.invoke(comp);
    }
  }

  private ExecutorService getExecutor(String name) {
    ExecutorService es = connectionStarters.get(name);
    if (es == null || es.isShutdown()) {
      es = Executors.newSingleThreadExecutor(new ManagedThreadFactory());
      connectionStarters.put(name, es);
    }
    return es;
  }

  private void shutdownExecutors() {
    for (ExecutorService es : connectionStarters.values()) {
      es.shutdownNow();
    }
  }

  public Boolean getThreadedStart() {
    return threadedStart;
  }

  /**
   * Enable use of {@link ExecutorService} to start connections.
   * 
   * @param b true to start all connections in their own thread default is null (false).
   */
  public void setThreadedStart(Boolean b) {
    this.threadedStart = b;
  }

  boolean threadedStart() {
    return getThreadedStart() != null ? getThreadedStart().booleanValue() : false;
  }

  public List<String> getIncludes() {
    return includes;
  }

  /**
   * Set the list of connection ids to start.
   * 
   * @param l the list of connection unique ids to include, defaults to an empty list.
   */
  public void setIncludes(List<String> l) {
    this.includes = Args.notNull(l, "includes");
  }

  public List<String> getExcludes() {
    return excludes;
  }

  /**
   * Set the list of connection ids to exclude.
   * 
   * @param l the list of connection unique ids to exclude, defaults to an empty list.
   */
  public void setExcludes(List<String> l) {
    this.excludes = Args.notNull(l, "excludes");
  }

  public void addInclude(String pattern) {
    getIncludes().add(pattern);
  }

  public void addExclude(String pattern) {
    getExcludes().add(pattern);
  }

  private void initialisePatterns() {
    if (includes.size() != includePatterns.size()) {
      includePatterns.clear();
      for (String regex : getIncludes()) {
        includePatterns.add(Pattern.compile(regex));
      }
    }
    if (excludes.size() != excludePatterns.size()) {
      excludePatterns.clear();
      for (String regex : getExcludes()) {
        excludePatterns.add(Pattern.compile(regex));
      }
    }
  }

  public Collection<AdaptrisConnection> filter(Collection<AdaptrisConnection> original) {
    return exclude(include(original));
  }

  private Collection<AdaptrisConnection> exclude(Collection<AdaptrisConnection> conns) {
    if (getExcludes().size() == 0) {
      return conns;
    }
    initialisePatterns();
    List<AdaptrisConnection> toBeRemoved = new ArrayList<>();
    for (AdaptrisConnection element : conns) {
      for (Pattern pattern : excludePatterns) {
        if (pattern.matcher(element.getUniqueId()).find()) {
          toBeRemoved.add(element);
          break;
        }
      }
    }
    conns.removeAll(toBeRemoved);
    return conns;
  }

  private Collection<AdaptrisConnection> include(Collection<AdaptrisConnection> conns) {
    if (getIncludes().size() == 0) {
      return conns;
    }
    initialisePatterns();
    List<AdaptrisConnection> result = new ArrayList<>();
    for (AdaptrisConnection element : conns) {
      for (Pattern pattern : includePatterns) {
        if (pattern.matcher(element.getUniqueId()).find()) {
          result.add(element);
          break;
        }
      }
    }
    return result;
  }

  private void logActivity(ConnectionAction action, Collection<AdaptrisConnection> conns) {
    if (log.isTraceEnabled()) {
      List<String> names = new ArrayList<>();
      for (AdaptrisConnection c : conns) {
        names.add(c.getUniqueId());
      }
      log.trace("Filtered list for {} operation : {}", action, names);
    }
  }
}
