package com.adaptris.core.management;


/**
 * Entry point into an adapter from the commandline.
 * <p>
 * Basically the same as StandardBootstrap but without the classpath initialization.
 * </p>
 *
 * @author gcsiki
 */
public class SimpleBootstrap extends StandardBootstrap {

  public SimpleBootstrap(String[] argv) throws Exception {
    super(argv);
  }

  @Override
  public void boot() throws Exception {
    logVersionInformation();
    super.standardBoot();
  }

  /**
   * <p>
   * Entry point to program.
   * </p>
   *
   * @param
   * 		argv - Command line arguments
   *
   * @throws Exception upon some unrecoverable error.
   */
  public static void main(String[] argv) throws Exception {
    new SimpleBootstrap(argv).boot();
  }
}
