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
   * @param p the properties
   * @return null or something that can start/stop/register itself.
   * @throws Exception
   */
  protected abstract JmxComponent createJmxWrapper(Properties p) throws Exception;

  @Override
  public final void init(Properties p) throws Exception {
    JmxComponent w = createJmxWrapper(p);
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
