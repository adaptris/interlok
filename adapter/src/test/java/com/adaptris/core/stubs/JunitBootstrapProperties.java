package com.adaptris.core.stubs;

import java.util.Properties;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.util.license.License;
import com.adaptris.util.license.LicenseException;

public class JunitBootstrapProperties extends BootstrapProperties {

  /**
   *
   */
  private static final long serialVersionUID = 2013111101L;

  public JunitBootstrapProperties(Properties p) {
    super(p);
  }

  @Override
  public License getLicense() throws LicenseException {
    return new LicenseStub();
  }
}
