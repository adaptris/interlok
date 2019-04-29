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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.util.text.DateFormatUtil;

@SuppressWarnings("deprecation")
public class ReformatDateServiceTest extends MetadataServiceExample {

  private static final String DEST_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";
  private static final String BAD_SOURCE_DATE_FORMAT = "HH:mm:ss zzz";
  private static final String DATE_METADATA_KEY = "dateMetadataKey";

  private static final String REGEXP_DEST_DATE_FORMAT = "^[0-9\\-]+ [0-9:]+.*$";
  private static final String REGEXP_DEST_DATE_NUMERIC = "^[0-9]+$";
  protected transient Logger myLogger = LoggerFactory.getLogger(this.getClass().getName());

  public ReformatDateServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String srcDateStr = new SimpleDateFormat(DateFormatBuilder.DEFAULT_DATE_FORMAT).format(new Date());
    msg.addMetadata(DATE_METADATA_KEY, srcDateStr);
    return msg;
  }

  public void testService_Defaults() throws Exception {
    ReformatDateService service = new ReformatDateService();
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    Date date = new SimpleDateFormat(DateFormatBuilder.DEFAULT_DATE_FORMAT).parse(msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertEquals(date, new SimpleDateFormat(DateFormatBuilder.DEFAULT_DATE_FORMAT).parse(msg.getMetadataValue(DATE_METADATA_KEY)));
  }

  public void testService() throws Exception {
    ReformatDateService service = new ReformatDateService();
    service.setSourceFormatBuilder(new DateFormatBuilder());
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    Date date = new SimpleDateFormat(DateFormatBuilder.DEFAULT_DATE_FORMAT).parse(msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertEquals(date, new SimpleDateFormat(DEST_DATE_FORMAT).parse(msg.getMetadataValue(DATE_METADATA_KEY)));
  }

  public void testService_WithTimezone_WithLanguage() throws Exception {
    ReformatDateService service = new ReformatDateService();
    DateFormatBuilder sourceFormatBuilder = new DateFormatBuilder().withLanguageTag(Locale.getDefault().getLanguage())
        .withTimezone(TimeZone.getDefault().getID()).withFormat(DateFormatBuilder.DEFAULT_DATE_FORMAT);
    service.setSourceFormatBuilder(sourceFormatBuilder);
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    Date date = new SimpleDateFormat(DateFormatBuilder.DEFAULT_DATE_FORMAT).parse(msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertEquals(date, new SimpleDateFormat(DEST_DATE_FORMAT).parse(msg.getMetadataValue(DATE_METADATA_KEY)));
  }

  public void testService_SourceFormat_SecondsEpoch() throws Exception {

    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setSourceFormatBuilder(new DateFormatBuilder(DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name()));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DATE_METADATA_KEY, secondsSinceEpoch().toString());
    myLogger.trace("{} BEFORE {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    myLogger.trace("{} AFTER {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));

    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DATE_METADATA_KEY).matches(REGEXP_DEST_DATE_FORMAT));
  }

  public void testService_SourceFormat_MillisecondsEpoch() throws Exception {

    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setSourceFormatBuilder(new DateFormatBuilder(DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name()));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DATE_METADATA_KEY, new BigDecimal(System.currentTimeMillis()).toString());
    myLogger.trace("{} BEFORE {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    myLogger.trace("{} AFTER {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));

    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DATE_METADATA_KEY).matches(REGEXP_DEST_DATE_FORMAT));
  }

  public void testService_SourceFormat_SecondsEpoch_SciNotation() throws Exception {

    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setSourceFormatBuilder(new DateFormatBuilder(DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name()));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DATE_METADATA_KEY, scientific(secondsSinceEpoch()));
    myLogger.trace("{} BEFORE {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    myLogger.trace("{} AFTER {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));

    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DATE_METADATA_KEY).matches(REGEXP_DEST_DATE_FORMAT));
  }

  public void testService_SourceFormat_MillisecondsEpoch_SciNotation() throws Exception {

    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setSourceFormatBuilder(new DateFormatBuilder(DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name()));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(DATE_METADATA_KEY, scientific(new BigDecimal(System.currentTimeMillis())));
    myLogger.trace("{} BEFORE {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));
    execute(service, msg);
    myLogger.trace("{} AFTER {}", getName(), msg.getMetadataValue(DATE_METADATA_KEY));

    assertTrue(msg.headersContainsKey(DATE_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DATE_METADATA_KEY).matches(REGEXP_DEST_DATE_FORMAT));
  }

  public void testService_DestFormat_SecondsEpoch() throws Exception {
    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DateFormatUtil.CustomDateFormat.SECONDS_SINCE_EPOCH.name()));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DATE_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DATE_METADATA_KEY).matches(REGEXP_DEST_DATE_NUMERIC));
  }

  public void testService_DestFormat_MillisecondsEpoch() throws Exception {
    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DateFormatUtil.CustomDateFormat.MILLISECONDS_SINCE_EPOCH.name()));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(DATE_METADATA_KEY));
    assertTrue(msg.getMetadataValue(DATE_METADATA_KEY).matches(REGEXP_DEST_DATE_NUMERIC));
  }

  public void testServiceWithBadFormat() throws Exception {
    ReformatDateService service = new ReformatDateService();
    service.setDestinationFormatBuilder(new DateFormatBuilder(DEST_DATE_FORMAT));
    service.setSourceFormatBuilder(new DateFormatBuilder(BAD_SOURCE_DATE_FORMAT));
    service.setMetadataKeyRegexp(DATE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected ReformatDateService retrieveObjectForSampleConfig() {
    return new ReformatDateService(".*matchingMetadataKeysContainingDates.*", new DateFormatBuilder(),
        new DateFormatBuilder(DEST_DATE_FORMAT).withTimezone("UTC").withLanguageTag("en_GB"));
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This can be used to reformat date strings stored in metadata.\n" + "-->\n";
  }

  private static String scientific(BigDecimal x) {
    NumberFormat formatter = new DecimalFormat("0.########E0");
    formatter.setRoundingMode(RoundingMode.HALF_UP);
    formatter.setMinimumFractionDigits(x.scale() > 0 ? x.precision() : x.scale());
    return formatter.format(x);
  }

  private static BigDecimal secondsSinceEpoch() {
    return new BigDecimal(System.currentTimeMillis()).divide(new BigDecimal(1000), RoundingMode.HALF_UP);
  }
}
