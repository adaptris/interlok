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

package com.adaptris.core.jms;

import javax.jms.Session;

/**
 * Utility class to convert a meaningful string into a javax.jms.Session
 * constant.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public class AcknowledgeMode {

  public enum Mode {
    /**
     * Maps to {@link javax.jms.Session#AUTO_ACKNOWLEDGE}
     *
     */
    AUTO_ACKNOWLEDGE {
      @Override
      int acknowledgeMode() {
        return Session.AUTO_ACKNOWLEDGE;
      }
    },
    /**
     * Maps to {@link javax.jms.Session#CLIENT_ACKNOWLEDGE}
     *
     */
    CLIENT_ACKNOWLEDGE {
      @Override
      int acknowledgeMode() {
        return Session.CLIENT_ACKNOWLEDGE;
      }
    },
    /**
     * Maps to {@link javax.jms.Session#DUPS_OK_ACKNOWLEDGE}
     *
     */
    DUPS_OK_ACKNOWLEDGE {
      @Override
      int acknowledgeMode() {
        return Session.DUPS_OK_ACKNOWLEDGE;
      }
    };
    abstract int acknowledgeMode();
  }

  /**
   * Get the appropriate AcknowledgeMode.
   * 
   * @param s a {@link AcknowledgeMode.Mode}, if unknown then treated as an
   *          integer.
   * @return the acknowledge mode
   */
  public static final int getMode(String s) {
    Mode mode = null;
    int result;
    try {
      mode = Mode.valueOf(s);
      result = mode.acknowledgeMode();
    }
    catch (IllegalArgumentException e) {
      result = Integer.parseInt(s);
    }
    return result;
  }
}
