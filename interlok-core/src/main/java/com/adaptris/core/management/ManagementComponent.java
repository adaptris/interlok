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

package com.adaptris.core.management;

import java.util.Properties;

/**
 * Interface for management components that exist outside of the standard adapter lifecycle.
 *
 *
 */
public interface ManagementComponent {

  default void setClassLoader(ClassLoader classLoader) {

  }

  /**
   * Initialise the management component.
   *
   * @param config
   *          configuration properties that have been built during bootstrap.
   * @throws Exception
   *           if initialisation fails.
   */
  public void init(Properties config) throws Exception;

  /**
   * Start the management component.
   *
   */
  public void start() throws Exception;

  /**
   * Stop the management component.
   *
   */
  public void stop() throws Exception;

  /**
   * Destroy the management component making it require re-initialisation.
   *
   */
  void destroy() throws Exception;

}
