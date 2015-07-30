/*
 * $RCSfile: AddTimestampMetadataServiceTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/06/03 19:05:02 $
 * $Author: lchan $
 */
package com.adaptris.core.services.metadata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.DateFormatUtil;

public class AddTimestampMetadataServiceTest extends MetadataServiceExample {

  private static final String DEFAULT_METADATA_KEY = "timestamp";
  private static final String DEFAULT_TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  private static final String KEY2 = "key2";
  private static final String KEY1 = DEFAULT_METADATA_KEY;
  private static final String REGEXP_DEST_DATE_NUMERIC = "^[0-9]+$";

  public AddTimestampMetadataServiceTest(String name) {
    super(name);
  }

  @Override
  public void setUp() {
  }

  public void testInitialisationCriteria() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    service.setDateFormat("");
    initWithException(service);
    service.setDateFormat(null);
    initWithException(service);

    service = new AddTimestampMetadataService();
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

  public void testBasicService() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    execute(service, m);
    assertTrue(m.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(m.getMetadataValue(DEFAULT_METADATA_KEY) != null);
  }

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

  public void testAlwaysReplace() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    m.addMetadata(DEFAULT_METADATA_KEY, "123456");
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    service.setAlwaysReplace(true);
    execute(service, m);
    assertTrue(m.containsKey(DEFAULT_METADATA_KEY));
    assertNotSame("123456", m.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testNotAlwaysReplace() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    m.addMetadata(DEFAULT_METADATA_KEY, "123456");
    AddTimestampMetadataService service = new AddTimestampMetadataService();
    service.setAlwaysReplace(false);
    execute(service, m);
    assertTrue(m.containsKey(DEFAULT_METADATA_KEY));
    assertEquals("123456", m.getMetadataValue(DEFAULT_METADATA_KEY));
  }

  public void testBug1552() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ServiceList sl = new ServiceList();
    sl.addService(new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, true));
    sl.addService(new AddTimestampMetadataService("yyyy-MM-dd HH:mm:ssZ", KEY2, true));
    execute(sl, m);
    assertTrue(m.containsKey(KEY1));
    assertTrue(m.containsKey(KEY2));

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
    String key2 = m.getMetadataValue(KEY2);
    try {
      sdf.parse(key2);
    }
    catch (ParseException e) {
      fail("Failed to parse " + key2);
    }
  }

  public void testOffset() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Date now = new Date();
    AddTimestampMetadataService service = new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, false, "P30D");
    execute(service, m);
    assertTrue(m.containsKey(KEY1));
    SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TS_FORMAT);
    String date1 = m.getMetadataValue(KEY1);
    try {
      Date d = sdf.parse(date1);
      assertTrue(d.after(now));
    }
    catch (ParseException e) {
    }
  }

  public void testEmptyOffset() throws Exception {
    AdaptrisMessage m = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    AddTimestampMetadataService service = new AddTimestampMetadataService(DEFAULT_TS_FORMAT, DEFAULT_METADATA_KEY, false, "");
    execute(service, m);
    assertTrue(m.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(m.getMetadataValue(DEFAULT_METADATA_KEY) != null);
  }

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

  public void testService_SecondsEpoch() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService(
        DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name(), DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DEFAULT_METADATA_KEY).matches(REGEXP_DEST_DATE_NUMERIC));
  }

  public void testService_MillisecondsEpoch() throws Exception {
    AddTimestampMetadataService service = new AddTimestampMetadataService(
        DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name(), DEFAULT_METADATA_KEY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DEFAULT_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DEFAULT_METADATA_KEY).matches(REGEXP_DEST_DATE_NUMERIC));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new AddTimestampMetadataService(DEFAULT_TS_FORMAT, KEY1, null, "P30D");
  }

}