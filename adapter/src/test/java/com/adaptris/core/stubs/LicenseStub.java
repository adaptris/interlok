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

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

import com.adaptris.util.license.LicenseException;

/**
 * License Stub to enable use to test without a real license.
 * @author lchan
 */
public class LicenseStub implements com.adaptris.util.license.License {

  private boolean isEnabled;
  private EnumSet<LicenseType> licenseSet;

  public LicenseStub() {
    this.licenseSet = EnumSet.of(LicenseType.Basic, LicenseType.Standard, LicenseType.Enterprise);
  }

  public LicenseStub(EnumSet<LicenseType> set) {
    this.licenseSet = set;
  }

  /**
   * @see com.adaptris.util.license.License#verify()
   */
  public void verify() throws LicenseException {
    return;
  }

  /**
   * @see com.adaptris.util.license.License#hasExpired()
   */
  public boolean hasExpired() throws LicenseException {
    return false;
  }

  /**
   * @see com.adaptris.util.license.License#getExpiry()
   */
  public Date getExpiry() throws LicenseException {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.YEAR, 1);
    return c.getTime();
  }

  @Override
  public boolean isEnabled(LicenseType type) {
    return licenseSet.contains(type);
  }

}
