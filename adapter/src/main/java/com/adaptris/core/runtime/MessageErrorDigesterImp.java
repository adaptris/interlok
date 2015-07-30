package com.adaptris.core.runtime;

import com.adaptris.core.CoreException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

/**
 * @author lchan
 *
 */
public abstract class MessageErrorDigesterImp implements MessageErrorDigester {

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isEnabled(License l) {
    return l.isEnabled(LicenseType.Standard);
  }
}
