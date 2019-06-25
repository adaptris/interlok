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

package com.adaptris.core.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.StateManagedComponent;

/**
 * Helper class that assists in managing component lifecycles.
 * 
 * @author lchan
 * 
 */
public abstract class LifecycleHelper {

  private static Logger log = LoggerFactory.getLogger(LifecycleHelper.class);

  private static enum Logging {
    Prepare, Start, Stop, Init, Close;
    
    void doLogging(ComponentLifecycle c) {
      log.trace("Executing {}(): {}", toString(), LoggingHelper.friendlyName(c));
    }
  }

  /**
   * Initialise the component.
   * 
   * @param s the component
   * @throws CoreException from {@link ComponentLifecycle#init()} or {@link StateManagedComponent#requestInit()}
   * @see ComponentLifecycle#init()
   * @see StateManagedComponent#requestInit()
   */
  public static final void init(ComponentLifecycle... s) throws CoreException {
    for (ComponentLifecycle c : s) {
      init(c, true);
    }
  }

  private static void init(ComponentLifecycle s, boolean logging) throws CoreException {
    if (s != null) {
      if (logging) Logging.Init.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestInit();
      } else {
        s.init();
      }
    }
  }

  /**
   * Start the component.
   * 
   * @param s the component
   * @throws CoreException from {@link ComponentLifecycle#start()} or {@link StateManagedComponent#requestStart()}
   * @see ComponentLifecycle#start()
   * @see StateManagedComponent#requestStart()
   */
  public static final void start(ComponentLifecycle... s) throws CoreException {
    for (ComponentLifecycle c : s) {
      start(c, true);
    }
  }

  private static void start(ComponentLifecycle s, boolean logging) throws CoreException {
    if (s != null) {
      if (logging) Logging.Start.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestStart();
      } else {
        s.start();
      }
    }
  }

  /**
   * Stop the component.
   * 
   * @param s the component
   * @see ComponentLifecycle#stop()
   * @see StateManagedComponent#requestStop()
   */
  public static final void stop(ComponentLifecycle... s) {
    for (ComponentLifecycle c : s) {
      stop(c, true);
    }
  }

  private static void stop(ComponentLifecycle s, boolean logging) {
    if (s != null) {
      if (logging) Logging.Stop.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestStop();
      } else {
        s.stop();
      }
    }
  }

  /**
   * Close the component.
   * 
   * @param s the component
   * @see ComponentLifecycle#close()
   * @see StateManagedComponent#requestClose()
   */
  public static final void close(ComponentLifecycle... s) {
    for (ComponentLifecycle c : s) {
      close(c, true);
    }
  }

  private static void close(ComponentLifecycle s, boolean logging) {
    if (s != null) {
      if (logging) Logging.Close.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestClose();
      } else {
        s.close();
      }
    }
  }

  /**
   * Register the event handler against a component that requires it.
   * 
   * @param c a service.
   * @param eh the event handler.
   */
  public static void registerEventHandler(AdaptrisComponent c, EventHandler eh) {
    if (c != null && c instanceof EventHandlerAware) {
      ((EventHandlerAware) c).registerEventHandler(eh);
    }
  }

  /**
   * Prepare the component if it can be.
   * 
   * @param s a service.
   * @throws CoreException
   */
  public static void prepare(ComponentLifecycle... s) throws CoreException {
    for (ComponentLifecycle c : s) {
      prepare(c, true);
    }
  }

  private static void prepare(ComponentLifecycle c, boolean logging) throws CoreException {
    if (c != null && c instanceof ComponentLifecycleExtension) {
      if (logging) Logging.Prepare.doLogging(c);
      ((ComponentLifecycleExtension) c).prepare();
    }
  }

  /**
   * Prepare initialise and start
   * 
   */
  public static <T extends ComponentLifecycle> T initAndStart(T c) throws CoreException {
    return initAndStart(c, true);
  }

  public static <T extends ComponentLifecycle> T initAndStart(T c, boolean logging) throws CoreException {
    LifecycleHelper.prepare(c, logging);
    LifecycleHelper.init(c, logging);
    try {
      LifecycleHelper.start(c, logging);
    } catch (CoreException e) {
      LifecycleHelper.close(c, logging);
      throw e;
    }
    return c;
  }

  /**
   * Stop and Close.
   * 
   */
  public static <T extends ComponentLifecycle> T stopAndClose(T c) {
    return stopAndClose(c, true);
  }

  public static <T extends ComponentLifecycle> T stopAndClose(T c, boolean logging) {
    LifecycleHelper.stop(c, logging);
    LifecycleHelper.close(c, logging);
    return c;
  }

  public static void waitQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException | IllegalArgumentException e) {
    }
  }
}
