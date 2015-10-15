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


/**
 * Utility class to convert a meaningful string into a javax.jms.DeliveryMode
 * constant.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public class DeliveryMode {

  public enum Mode {
    /**
     * Maps to {@link javax.jms.DeliveryMode#PERSISTENT}
     *
     */
    PERSISTENT {
      @Override
      int deliveryMode() {
        return javax.jms.DeliveryMode.PERSISTENT;
      }
    },
    /**
     * Maps to {@link javax.jms.DeliveryMode#NON_PERSISTENT}
     *
     */
    NON_PERSISTENT {
      @Override
      int deliveryMode() {
        return javax.jms.DeliveryMode.NON_PERSISTENT;
      }
    };
    abstract int deliveryMode();
  }

  /**
   * Get the appropriate DeliveryMode.
   * 
   * @param s a {@link DeliveryMode.Mode}, if unknown then treated as an
   *          integer.
   * @return the delivery mode
   */
  public static final int getMode(String s) {
    Mode mode = null;
    int result;
    try {
      mode = Mode.valueOf(s);
      result = mode.deliveryMode();
    }
    catch (IllegalArgumentException e) {
      result = Integer.valueOf(s).intValue();
    }
    return result;
  }
}
