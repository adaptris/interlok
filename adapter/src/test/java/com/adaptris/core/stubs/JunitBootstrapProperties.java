/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
