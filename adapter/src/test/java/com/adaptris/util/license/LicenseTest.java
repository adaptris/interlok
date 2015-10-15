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

import java.util.Date;

import com.adaptris.core.BaseCase;
import com.adaptris.core.stubs.ExternalResourcesHelper;
import com.adaptris.util.URLString;
import com.adaptris.util.license.License.LicenseType;

public class LicenseTest extends BaseCase {

  public static final String KEY_LICENSE_URL = "license.url";
  public static final String KEY_LICENSE_FILE = "license.file";

  public LicenseTest(String s) {
    super(s);
  }

  public void testCreateLicenseFromFile() throws Exception {
    License l = LicenseFactory.getLicense(PROPERTIES.getProperty(KEY_LICENSE_FILE));
    // Ok, we know this license has expired
    assertTrue(l.hasExpired());
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
  }

  public void testCreateLicenseFromClasspath() throws Exception {
    License l = LicenseFactory.getLicense("license.properties");
    // Ok, we know this license has expired
    assertTrue(l.hasExpired());
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
    assertFalse(l.isEnabled(LicenseType.Restricted));
  }

  public void testCreateLicenseFromUrl() throws Exception {
    String url = PROPERTIES.getProperty(KEY_LICENSE_URL);
    if (!ExternalResourcesHelper.isExternalServerAvailable(new URLString(url))) {
      log.debug(url + " not available, ignoring test  testCreateLicenseFromUrl");
      return;
    }
    License l = LicenseFactory.getLicense(url);
    // Ok, we know this license has expired
    assertTrue(l.hasExpired());
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
    assertFalse(l.isEnabled(LicenseType.Restricted));
  }

  public void testCreateLicenseFromBadUrl() throws Exception {
    License l = LicenseFactory.getLicense("lic.properties");
    // This is a RestrictedLicense
    assertFalse(l.hasExpired());
    assertFalse(new Date().after(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
    assertTrue(l.isEnabled(LicenseType.Restricted));
  }

  public void testCreateLicenseFromString() throws Exception {
    License l = LicenseFactory.create("ROq1xIbZkgwtHBypIQXAaA==");
    // Expired License.
    assertTrue(l.hasExpired());
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
    assertFalse(l.isEnabled(LicenseType.Restricted));
  }

  public void testCreateLicenseFromEmptyString() throws Exception {
    License l = LicenseFactory.create("");
    // This is a RestrictedLicense
    assertFalse(l.hasExpired());
    assertFalse(new Date().after(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
    assertTrue(l.isEnabled(LicenseType.Restricted));
  }

  public void testCreateLicenseFromNullString() throws Exception {
    License l = LicenseFactory.create(null);
    // This is a RestrictedLicense
    assertFalse(l.hasExpired());
    assertFalse(new Date().after(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
    assertTrue(l.isEnabled(LicenseType.Restricted));
  }

  public void testCreateLicenseFromBadString() throws Exception {
    try {
      License l = LicenseFactory.create("BlahBlahBlah");
      fail("Created license from BlahBlahBlah");
    }
    catch (LicenseException expected) {
      ;
    }
  }
}
