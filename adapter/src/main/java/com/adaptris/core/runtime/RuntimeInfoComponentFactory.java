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

package com.adaptris.core.runtime;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.CoreException;

public abstract class RuntimeInfoComponentFactory {

  protected static final Logger log = LoggerFactory.getLogger(RuntimeInfoComponentFactory.class);

  private static final Collection<RuntimeInfoComponentFactory> factories = new ArrayList<RuntimeInfoComponentFactory>();

  public static final void registerComponentFactory(RuntimeInfoComponentFactory factory) {
    synchronized (factories) {
      factories.add(factory);
    }
  }

  public static final RuntimeInfoComponent create(ParentRuntimeInfoComponent parent, AdaptrisComponent c) throws CoreException {
    RuntimeInfoComponent result = null;
    try {
      for (RuntimeInfoComponentFactory f : factories) {
        if (f.isSupported(c)) {
          result = f.createComponent(parent, c);
          break;
        }
      }
    }
    catch (Exception e) {
    }
    if (result == null && c != null) {
      log.trace("No RuntimeInfoComponent for " + c.getClass());
    }
    return result;
  }

  protected abstract boolean isSupported(AdaptrisComponent e);

  protected abstract RuntimeInfoComponent createComponent(ParentRuntimeInfoComponent parent, AdaptrisComponent e) throws Exception;

}
