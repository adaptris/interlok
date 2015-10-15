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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.adaptris.util.license.License.LicenseType;

public class SimpleLicenseTest {


  @Test
  public void testLicenseWithSpaces() throws Exception {
    SimpleLicenseCreator creator = new SimpleLicenseCreator(LicenseType.Basic, nextYear());
    SimpleLicense l = new SimpleLicense(spacify(creator.create()));
    l.verify();
    assertFalse(l.hasExpired());
    assertTrue(new Date().before(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertFalse(l.isEnabled(LicenseType.Standard));
    assertFalse(l.isEnabled(LicenseType.Enterprise));

  }

  @Test
  public void testBaseLicense() throws Exception {
    SimpleLicenseCreator creator = new SimpleLicenseCreator(LicenseType.Basic, nextYear());
    SimpleLicense l = new SimpleLicense(creator.create());
    l.verify();
    assertFalse(l.hasExpired());
    assertTrue(new Date().before(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertFalse(l.isEnabled(LicenseType.Standard));
    assertFalse(l.isEnabled(LicenseType.Enterprise));
  }

  @Test
  public void testStandardLicense() throws Exception {
    SimpleLicenseCreator creator = new SimpleLicenseCreator(LicenseType.Standard, nextYear());
    SimpleLicense l = new SimpleLicense(creator.create());
    l.verify();
    assertFalse(l.hasExpired());
    assertTrue(new Date().before(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertFalse(l.isEnabled(LicenseType.Enterprise));
  }

  @Test
  public void testEnterpriseLicense() throws Exception {
    SimpleLicenseCreator creator = new SimpleLicenseCreator(LicenseType.Enterprise, nextYear());
    SimpleLicense l = new SimpleLicense(creator.create());
    l.verify();
    assertFalse(l.hasExpired());
    assertTrue(new Date().before(l.getExpiry()));
    assertTrue(l.isEnabled(LicenseType.Basic));
    assertTrue(l.isEnabled(LicenseType.Standard));
    assertTrue(l.isEnabled(LicenseType.Enterprise));
  }

  @Test
  public void testExpiredLicense() throws Exception {
    SimpleLicenseCreator creator = new SimpleLicenseCreator(LicenseType.Basic, lastYear());
    SimpleLicense l = new SimpleLicense(creator.create());
    assertTrue(l.hasExpired());
    assertTrue(new Date().after(l.getExpiry()));
    try {
      l.verify();
      fail();
    } catch (LicenseException expected) {
      assertEquals("License has expired", expected.getMessage());
    }
  }

  private static Date nextYear() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, 1);
    return cal.getTime();
  }

  private static Date lastYear() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -1);
    return cal.getTime();
  }

  private static String spacify(String licenseKey) {
    StringBuilder sb = new StringBuilder(licenseKey);
    int groups = licenseKey.length() / 4;
    for (int i = 0; i < licenseKey.length() + groups; i += 5) {
      sb.insert(i, " ");
    }
    return sb.toString().trim();
  }

}
