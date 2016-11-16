/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.management.jmx;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.ManagementComponent;

abstract class JmxComponentImpl implements ManagementComponent {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient JmxComponent wrapper = new JmxComponent() {

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void unregister() throws Exception {
    }

    @Override
    public void register() throws Exception {
    }
  };

  /**
   * Create a JMX wrapper.
   *
   * @param p
   *          the properties
   * @return null or something that can start/stop/register itself.
   * @throws Exception
   */
  protected abstract JmxComponent createJmxWrapper(Properties p) throws Exception;

  @Override
  public void setClassLoader(final ClassLoader classLoader) {
    log.warn("Ignoring setClassLoader");
  }

  @Override
  public final void init(final Properties p) throws Exception {
    final JmxComponent w = createJmxWrapper(p);
    wrapper = w == null ? wrapper : w;
    wrapper.register();
  }

  @Override
  public final void start() throws Exception {
    wrapper.start();
  }

  @Override
  public final void stop() throws Exception {
    wrapper.stop();
  }

  @Override
  public final void destroy() throws Exception {
    wrapper.unregister();
  }

  protected abstract class JmxComponent {
    /**
     * Start the component.
     *
     * @throws Exception
     */
    public abstract void start() throws Exception;

    /**
     * Stop the component.
     *
     * @throws Exception
     */
    public abstract void stop() throws Exception;

    /**
     * Register the component in JMX
     *
     * @throws Exception
     */
    public abstract void unregister() throws Exception;

    /**
     * Unregister the component from JMX.
     *
     * @throws Exception
     */
    public abstract void register() throws Exception;
  }
}
