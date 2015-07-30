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
      result = Integer.valueOf(s).intValue();
    }
    return result;
  }
}
