package com.adaptris.core.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;

public abstract class MessageValidatorImpl implements MessageValidator {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {

  }

  @Override
  public void close() {

  }

}
