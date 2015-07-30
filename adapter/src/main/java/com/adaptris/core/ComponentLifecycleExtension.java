package com.adaptris.core;


/**
 * Extensions on the standard component lifecycle.
 *
 * @author lchan
 *
 */
public interface ComponentLifecycleExtension {

  /**
   * Prepare for initialisation.
   *
   * @throws CoreException
   */
  void prepare() throws CoreException;
}
