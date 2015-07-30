package com.adaptris.core;

import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;


/**
 * <p>
 * Contains behaviour common to <code>BranchingService</code>s.
 * </p>
 */
public abstract class BranchingServiceImp extends ServiceImp {

  /** @see com.adaptris.core.Service#isBranching() */
  @Override
  public boolean isBranching() {
    return true;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }

}
