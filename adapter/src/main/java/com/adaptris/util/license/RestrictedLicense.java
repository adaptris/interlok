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

package com.adaptris.util.license;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A License that isn't licensed.
 */
final class RestrictedLicense implements License {

  private transient Logger logR = LoggerFactory.getLogger(License.class);

  private transient Date expiryDate;

  RestrictedLicense() {
    expiryDate = twoWeeksFromNow();
  }


  @Override
  public boolean isEnabled(LicenseType type) {
    return true;
  }
  /**
   *
   * @see com.adaptris.util.license.License#verify()
   */
  public void verify() throws LicenseException {
    return;
  }

  /**
   *
   * @see com.adaptris.util.license.License#hasExpired()
   */
  public boolean hasExpired() throws LicenseException {
    return new Date().after(expiryDate);
  }

  /**
   *
   * @see com.adaptris.util.license.License#getExpiry()
   */
  public Date getExpiry() throws LicenseException {
    return expiryDate;
  }

  private static final Date twoWeeksFromNow() {
    Calendar c = Calendar.getInstance();
    // Unlicensed License always expires in 2 weeks time...
    c.add(Calendar.DAY_OF_YEAR, 14);
    return c.getTime();
  }


  @Override
  public String toString() {
    return "RESTRICTED_DEMO_LICENSE";
  }
}
