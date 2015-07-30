package com.adaptris.core.management;


/**
 * Provides backward compatibility for wrappers that explicitly use a main class.
 * 
 * @deprecated since 2.8.1 use {@link StandardBootstrap} instead.
 */
@Deprecated
public final class BootstrapLoader {

  /**
   * Provides a backward compatibility for previous versions.
   *
   * @param argv
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    new StandardBootstrap(argv).boot();
  }

}
