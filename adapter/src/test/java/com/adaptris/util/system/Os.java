package com.adaptris.util.system;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple querying of the operating system that the JVM is running on.
 *
 * @author $Author: lchan $
 * @author lchan
 */
public final class Os {

  /** Generic Windows platform */
  public static final String WINDOWS_FAMILY = "windows";
  /** Generic Unix */
  public static final String UNIX_FAMILY = "unix";
  /** Generic Windows NT style (NT, 2000, XP) */
  public static final String WINDOWS_NT_FAMILY = "winnt";
  /** Generic Windows 9x (95 / 98 / ME) */
  public static final String WINDOWS_9X_FAMILY = "win9x";

  /** Windows NT */
  public static final String WINDOWS_NT = "windows nt";
  /** Windows 2000 */
  public static final String WINDOWS_2K = "windows 2000";
  /** Windows 2003 */
  public static final String WINDOWS_2003 = "windows 2003";
  /** Windows 2008 */
  public static final String WINDOWS_2008 = "windows 2008";
  /** Windiws Vista */
  public static final String WINDOWS_VISTA = "windows vista";
  /** Windows 7 */
  public static final String WINDOWS_7 = "windows 7";
  /** Windows 8 */
  public static final String WINDOWS_8 = "windows 8";
  /** Windows 10 */
  public static final String WINDOWS_10 = "windows 10";
  /** Windows 2012 */
  public static final String WINDOWS_2012 = "windows 2012";
  /** Windows XP */
  public static final String WINDOWS_XP = "windows xp";
  /** Windows 95 */
  public static final String WINDOWS_95 = "windows 95";
  /** Windows 98 */
  public static final String WINDOWS_98 = "windows 98";
  /** Windows ME */
  public static final String WINDOWS_ME = "windows me";
  /** Linux */
  public static final String LINUX = "linux";
  /** Solaris TODO, find out what Solaris reports as the os.name! */
  public static final String SOLARIS = "sunos";
  /** HP-UX TODO, find out what HPUX reports as the os.name! */
  public static final String HPUX = "hp-ux";
  /** OS/2 */
  public static final String OS2 = "os/2";

  public static final String MAC_OSX = "mac os x";
  private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

  private static String[] UNIX_ARRAY =
  {
      LINUX, SOLARIS, HPUX, MAC_OSX, UNIX_FAMILY
  };

  private static String[] WINDOWS_NT_ARRAY = {
      WINDOWS_NT, WINDOWS_2K, WINDOWS_2003, WINDOWS_VISTA, WINDOWS_2008, WINDOWS_7, WINDOWS_8, WINDOWS_2012, WINDOWS_10
  };

  private static String[] WINDOWS_9X_ARRAY =
  {
      WINDOWS_95, WINDOWS_98, WINDOWS_ME
  };

  private static final String[] OS_FAMILIES =
  {
      UNIX_FAMILY, WINDOWS_NT_FAMILY, WINDOWS_9X_FAMILY, WINDOWS_FAMILY
  };

  private static final List<String> UNIX_LIST;
  private static final List<String> WINDOWS_NT_LIST;
  private static final List<String> WINDOWS_9X_LIST;

  static {
    UNIX_LIST = Collections.unmodifiableList(Arrays.asList(UNIX_ARRAY));
    WINDOWS_NT_LIST = Collections.unmodifiableList(Arrays.asList(WINDOWS_NT_ARRAY));
    WINDOWS_9X_LIST = Collections.unmodifiableList(Arrays.asList(WINDOWS_9X_ARRAY));
  }

  private Os() {
  }

  /**
   * Query whether the operating system is of a specific value.
   * <p>
   * This is not a particularly sophisticated test, and simply relies on the
   * presence of the string in the system property <code>os.name</code>
   *
   * @param os the os to test for
   * @return true if there is a match
   */
  public static boolean isOs(String os) {
    if (os == null || os.equals("")) {
      return false;
    }
    return OS_NAME.indexOf(os) > -1;
  }

  /**
   * Query the Generic type of operating system.
   *
   * @param os the type of Os
   * @return true or false, depending.
   * @see #UNIX_FAMILY
   * @see #WINDOWS_FAMILY
   * @see #WINDOWS_NT_FAMILY
   */
  public static boolean isFamily(String os) {
    if (os == null || os.equals("")) {
      return false;
    }
    if (os.equals(WINDOWS_FAMILY)) {
      return isOs(WINDOWS_FAMILY);
    }
    if (os.equals(WINDOWS_NT_FAMILY)) {
      for (String s : WINDOWS_NT_LIST) {
        if (isOs(s)) {
          return true;
        }
      }
    }
    if (os.equals(WINDOWS_9X_FAMILY)) {
      for (String s : WINDOWS_9X_LIST) {
        if (isOs(s)) {
          return true;
        }
      }
    }
    if (os.equals(UNIX_FAMILY)) {
      for (String s : UNIX_LIST) {
        if (isOs(s)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get the OS family that this JVM is running on.
   *
   * @return the family, if no family could be determined then the value of the
   *         system property <code>os.name</code> is returned.
   * @see #UNIX_FAMILY
   * @see #WINDOWS_FAMILY
   * @see #WINDOWS_NT_FAMILY
   */
  public static String getFamily() {
    for (int i = 0; i < OS_FAMILIES.length; i++) {
      if (isFamily(OS_FAMILIES[i])) {
        return OS_FAMILIES[i];
      }
    }
    return OS_NAME;
  }

}
