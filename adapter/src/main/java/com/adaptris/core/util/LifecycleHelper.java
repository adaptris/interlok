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
  public static final void init(ComponentLifecycle s) throws CoreException {
    if (s != null) {
      Logging.Init.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestInit();
      }
      else {
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
  public static final void start(ComponentLifecycle s) throws CoreException {
    if (s != null) {
      Logging.Start.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestStart();
      }
      else {
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
  public static final void stop(ComponentLifecycle s) {
    if (s != null) {
      Logging.Stop.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestStop();
      }
      else {
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
  public static final void close(ComponentLifecycle s) {
    if (s != null) {
      Logging.Close.doLogging(s);
      if (s instanceof StateManagedComponent) {
        ((StateManagedComponent) s).requestClose();
      }
      else {
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
   * @param c a service.
   * @throws CoreException
   */
  public static void prepare(ComponentLifecycle c) throws CoreException {
    if (c != null && c instanceof ComponentLifecycleExtension) {
      Logging.Prepare.doLogging(c);
      ((ComponentLifecycleExtension) c).prepare();
    }
  }

  /**
   * Prepare initialise and start
   * 
   */
  public static <T extends ComponentLifecycle> T initAndStart(T c) throws CoreException {
    LifecycleHelper.prepare(c);
    LifecycleHelper.init(c);
    try {
      LifecycleHelper.start(c);
    } catch (CoreException e) {
      LifecycleHelper.close(c);
      throw e;
    }
    return c;
  }

  /**
   * Stop and Close.
   * 
   */
  public static <T extends ComponentLifecycle> T stopAndClose(T c) {
    LifecycleHelper.stop(c);
    LifecycleHelper.close(c);
    return c;
  }

  public static void waitQuietly(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException | IllegalArgumentException e) {
    }
  }
}
