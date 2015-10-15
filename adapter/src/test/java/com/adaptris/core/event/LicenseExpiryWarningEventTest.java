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

package com.adaptris.core.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.DefaultMarshaller;

public class LicenseExpiryWarningEventTest {
  private static final SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static String LEGACY_LICENSE_EVENT =
      "<license-expiry-warning-event>" +
      "<name-space>event.ale.license.expiry.success</name-space>" +
      "<license-expiry>2015-05-10</license-expiry>" +
      "<creation-time>1431081667343</creation-time>" +
      "<unique-id>2deda477-84fe-4950-bda1-3a597656261f</unique-id>" +
      "<adapter-unique-id>gdltstadapterptp</adapter-unique-id>" +
      "<was-successful>true</was-successful>" +
      "<source-id>gdltstadapterptp</source-id>" +
      "</license-expiry-warning-event>";
  
  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testLicenseExpiry() throws Exception {
    LicenseExpiryWarningEvent evt = new LicenseExpiryWarningEvent();
    assertEquals(today(), evt.getLicenseExpiry());
    Date d = evt.when();
    Date expiryDate = evt.getExpiryDate();
    assertEquals(d, expiryDate);
    assertTrue(DateUtils.isSameDay(new Date(), d));
    assertTrue(DateUtils.isSameDay(new Date(), expiryDate));
  }

  public void testLegacyUnmarshal() throws Exception {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    LicenseExpiryWarningEvent evt = (LicenseExpiryWarningEvent) m.unmarshal(LEGACY_LICENSE_EVENT);
    assertEquals("2015-05-10", evt.getLicenseExpiry());
    // Legacy unmarshal won't have touched the expiryDate.
    assertTrue(DateUtils.isSameDay(new Date(), evt.getExpiryDate()));
    // Legacy Unmarshal will of course, change then "when()".
    assertFalse(DateUtils.isSameDay(new Date(), evt.when()));
  }

  private static final String today() {
    Date d = new Date();
    return standardFormat.format(d);
  }

}
