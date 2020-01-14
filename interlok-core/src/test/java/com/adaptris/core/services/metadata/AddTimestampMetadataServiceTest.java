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

package com.adaptris.core.services.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.metadata.timestamp.LastMessageTimestampGenerator;
import com.adaptris.core.services.metadata.timestamp.OffsetTimestampGenerator;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.DateFormatUtil;

public class AddTimestampMetadataServiceTest extends MetadataServiceExample {

  private static final String DEFAULT_METADATA_KEY = "timestamp";
  private static final String DEFAULT_TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  private static final String KEY2 = "key2";
  private static final String KEY1 = DEFAULT_METADATA_KEY;
  private static final String REGEXP_DEST_DATE_NUMERIC = "^[0-9]+$";
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testInitialisationCriteria() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService();

    service.setMetadataKey("");
    initWithException(service);
    service.setMetadataKey(null);
    initWithException(service);

    service = new AddTimestampMetadataService();
    LifecycleHelper.init(service);
  }

  private void initWithException(AddTimestampMetadataService s) {
    try {
      LifecycleHelper.init(s);
      fail("Should not initialise : " + s);
    }
    catch (CoreException e) {
      ;
    }
  }

  @Test
  public void testService() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    execute(service, m);
    assertTrue(m.headersContainsKey(DEFAULT_METADATA_KEY));
    assertTrue(m.getMetadataValue(DEFAULT_METADATA_KEY) != null);
  }

  @Test
  public void testService_Defaults() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    service.setDateFormatBuilder(null);
    execute(service, m);
    assertTrue(m.headersContainsKey(DEFAULT_METADATA_KEY));
    assertTrue(m.getMetadataValue(DEFAULT_METADATA_KEY) != null);
  }

  @Test
  public void testSetAlwaysReplace() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    assertNull(service.getAlwaysReplace());
    assertFalse(service.alwaysReplace());
    service.setAlwaysReplace(Boolean.TRUE);
    assertEquals(Boolean.TRUE, service.getAlwaysReplace());
    assertEquals(true, service.alwaysReplace());
    service.setAlwaysReplace(null);
    assertNull(service.getAlwaysReplace());
    assertFalse(service.alwaysReplace());
  }

  @Test
  public void testAlwaysReplace() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    m.addMetadata(DEFAULT_METADATA_KEY, "123456");
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    service.setAlwaysReplace(true);
    execute(service, m);
    assertTrue(m.headersContainsKey(DEFAULT_METADATA_KEY));
    assertNotSame("123456", m.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testNotAlwaysReplace() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    m.addMetadata(DEFAULT_METADATA_KEY, "123456");
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    service.setAlwaysReplace(false);
    execute(service, m);
    assertTrue(m.headersContainsKey(DEFAULT_METADATA_KEY));
    assertEquals("123456", m.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  @Test
  public void testBug1552() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ServiceList sl = new ServiceList();
    sl.addService(new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, true));
    sl.addService(new AddTimestampMetadataService("yyyy-MM-dd HH:mm:ssZ", KEY2, true));
    execute(sl, m);
    assertTrue(m.headersContainsKey(KEY1));
    assertTrue(m.headersContainsKey(KEY2));

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
    String key2 = m.getMetadataValue(KEY2);
    try {
      sdf.parse(key2);
    }
    catch (ParseException e) {
      fail("Failed to parse " + key2);
    }
  }

  @Test
  public void testOffset() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Date now = new Date();
    AddTimestampMetadataService service = new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, false, "P30D");
    execute(service, m);
    assertTrue(m.headersContainsKey(KEY1));
    SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TS_FORMAT);
    String date1 = m.getMetadataValue(KEY1);
    try {
      Date d = sdf.parse(date1);
      assertTrue(d.after(now));
    } catch (ParseException e) {
    }
  }

  @Test
  public void testLastMessage() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AddTimestampMetadataService service =
        new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, false, new LastMessageTimestampGenerator());
    execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    Thread.sleep(1000);
    Date now = new Date();
    execute(service, m);
    assertTrue(m.headersContainsKey(KEY1));
    SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TS_FORMAT);
    String date1 = m.getMetadataValue(KEY1);
    try {
      Date d = sdf.parse(date1);
      assertTrue(d.before(now));
    } catch (ParseException e) {
    }
  }

  @Test
  public void testEmptyOffset() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AddTimestampMetadataService service =
        new AddTimestampMetadataService(DEFAULT_TS_FORMAT, DEFAULT_METADATA_KEY, false, new OffsetTimestampGenerator(""));
    execute(service, m);
    assertTrue(m.headersContainsKey(DEFAULT_METADATA_KEY));
    assertTrue(m.getMetadataValue(DEFAULT_METADATA_KEY) != null);
  }

  @Test
  public void testInvalidOffset() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Date now = new Date();
    AddTimestampMetadataService service = new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, false, "BLAHBLAH");
    try {
      execute(service, m);
      fail("Service success with BLAHBLAH as the offset");
    }
    catch (ServiceException expected) {
      ;
    }
  }

  @Test
  public void testService_SecondsEpoch() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService(
        DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name(), DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertTrue(msg.headersContainsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DEFAULT_METADATA_KEY).matches(REGEXP_DEST_DATE_NUMERIC));
  }

  @Test
  public void testService_MillisecondsEpoch() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService(
        DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name(), DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertTrue(msg.headersContainsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DEFAULT_METADATA_KEY).matches(REGEXP_DEST_DATE_NUMERIC));
  }

  @Override
  protected AddTimestampMetadataService retrieveObjectForSampleConfig() {
    return new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, null, "P30D");
  }

}
