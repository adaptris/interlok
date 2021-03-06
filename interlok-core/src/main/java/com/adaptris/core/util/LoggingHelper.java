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

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.Service;
import com.adaptris.core.StateManagedComponent;

/**
 * Utility for generating logging messages.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class LoggingHelper {

  private static final String GUID_PATTERN = "^[0-9a-f]{8}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{4}\\-[0-9a-f]{12}$";
  private static final Logger log = LoggerFactory.getLogger(LoggingHelper.class);

  public static void logDeprecation(boolean alreadyLogged, WarningLoggedCallback callback, String old, String replacement) {
    logWarning(alreadyLogged, callback, "[{}] is deprecated, use [{}] instead", old, replacement);
  }

  public static void logWarning(boolean alreadyLogged, WarningLoggedCallback callback, String text, Object...args) {
    if (!alreadyLogged) {
      log.warn(text, args);
      callback.warningLogged();
    }
  }

  public static String friendlyName(Service s) {
    if (s == null) {
      return "";
    }
    // return s.getClass().getSimpleName() + "(" + (isBlank(s.getUniqueId()) ? "" : s.getUniqueId()) + ")";
    return s.getClass().getSimpleName() + filterGuid(s.getUniqueId());
  }

  public static String friendlyName(StateManagedComponent comp) {
    if (comp == null) {
      return "";
    }
    // return comp.getClass().getSimpleName() + "(" + (isBlank(comp.getUniqueId()) ? "" : comp.getUniqueId()) + ")";
    return comp.getClass().getSimpleName() + filterGuid(comp.getUniqueId());
  }

  public static String friendlyName(ComponentLifecycle comp) {
    if (comp == null) {
      return "";
    }
    return comp.getClass().getSimpleName() + filterGuid(reflectiveUniqueID(comp));
  }

  public static String reflectiveUniqueID(Object comp) {
    String result = "";
    try {
      Method m = comp.getClass().getMethod("getUniqueId");
      result = m.invoke(comp).toString();
    }
    catch (Exception e) {
    }
    return result;
  }

  public static String filterGuid(String uid) {
    if (isBlank(uid) || uid.matches(GUID_PATTERN)) {
      return "";
    }
    return "(" + uid + ")";
  }

  @FunctionalInterface
  public interface WarningLoggedCallback {
    void warningLogged();
  }
}
