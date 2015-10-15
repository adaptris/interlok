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

package com.adaptris.core.management.vcs;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeVersionControlLoader {

  private static RuntimeVersionControlLoader INSTANCE;

  private transient Logger log = LoggerFactory.getLogger(RuntimeVersionControlLoader.class);
  private ServiceLoader<RuntimeVersionControl> runtimeVersionControls;

  private RuntimeVersionControlLoader() {
    runtimeVersionControls = ServiceLoader.load(RuntimeVersionControl.class);
  }

  public static RuntimeVersionControlLoader getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new RuntimeVersionControlLoader();
    }
    return INSTANCE;
  }

  public RuntimeVersionControl load() {
    RuntimeVersionControl returnedVersionControlSystem = null;

    int systemsFound = 0;
    for(RuntimeVersionControl vcs : runtimeVersionControls) {
      systemsFound ++;
      log.info("Found version control system for " + vcs.getImplementationName());
      returnedVersionControlSystem = vcs;
    }

    if(systemsFound == 0) {
      log.info("No version control systems found.");
      return null;
    } else if(systemsFound == 1) {
      return returnedVersionControlSystem;
    } else {
      log.info("Multiple version control systems found, using " + returnedVersionControlSystem.getImplementationName());
      return returnedVersionControlSystem;
    }
  }

  /**
   * <p>
   * This static method will return all available {@link RuntimeVersionControl}s.
   * </p>
   * <p>
   * Availability depends on which RuntimeVersionControl jar files are in your installations lib directory.
   * </p>
   * @return A list of {@link RuntimeVersionControl}
   */
  public List<RuntimeVersionControl> availableImplementations() {
    List<RuntimeVersionControl> impls = new ArrayList<>();

    for(RuntimeVersionControl vcs : runtimeVersionControls) {
      impls.add(vcs);
    }

    return impls;
  }

  /**
   * <p>
   * Will load a specific {@link RuntimeVersionControl} given the exact implementation name. The
   * implementation name must match exactly with any given
   * {@link RuntimeVersionControl#getImplementationName()}.
   * </p>
   * <p>
   * If we cannot find the RuntimeVersionControl based on the given implementation name or if there
   * are no RuntimeVersionControl available, this method will return null.
   * </p>
   * 
   * @param implementationName
   * @return {@link RuntimeVersionControl}
   */
  public RuntimeVersionControl load(String implementationName) {
    RuntimeVersionControl returnedVcs = null;
    List<RuntimeVersionControl> availableImplementations = availableImplementations();
    for(RuntimeVersionControl vcs : availableImplementations) {
      if(vcs.getImplementationName().equals(implementationName)) {
        returnedVcs = vcs;
        break;
      }
    }

    return returnedVcs;
  }

}
