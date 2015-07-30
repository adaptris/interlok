package com.adaptris.core;

import com.adaptris.util.license.License;

/**
 * Interface defining Licensing requirements.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface LicensedComponent {

  /**
   * <p>
   * Verifies that this <code>AdaptrisComponent</code> is enabled based
   * on the current <code>License</code>.  This may be split out to a
   * separate interface if required.
   * </p>
   * @param license the current <code>License</code> object
   * @return true if the license allows this component to be used
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  boolean isEnabled(License license) throws CoreException;
}
