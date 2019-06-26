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

package com.adaptris.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import com.adaptris.util.text.Base58;

public class GuidGeneratorWithTimeTest extends GuidGeneratorWithTime {

  private static final String DATE_FORMAT_WITH_TIME = "yyyy-MM-dd HH:mm:ss";
  private static final String DATE_FORMAT_DAY = "yyyy-MM-dd";
  private static final String END_OF_DAY = " 23:59:59";
  private static final String START_OF_DAY = " 00:00:00";

  @Test
  public void testGetUUID() throws Exception {
    GuidGeneratorWithTime guid = new GuidGeneratorWithTime();
    assertNotNull(guid.getUUID());
  }

  @Test
  public void testCreateId() throws Exception {
    GuidGeneratorWithTime guid = new GuidGeneratorWithTime();
    Object o = new Object();
    String id1 = guid.create(o);
    String id2 = guid.create(o);
    assertNotSame(id1, id2);
    assertNotNull(guid.create(null));
  }

  @Test
  public void testRange_WithoutCompute() throws Exception {
    // Since we have a possible "range" depending on the time component, let's use the
    // first 2 characters of the the generated ID to make sure we are at least
    // bounded.
    GuidGeneratorWithTime guid = new GuidGeneratorWithTime();
    String id = guid.getUUID();
    String startingId = generateRangeBoundary(true);
    String endingId = generateRangeBoundary(false);
    System.err.println("Range for today is : " + startingId + " to " + endingId);
    assertTrue(id.startsWith(startingId.substring(0, 2)));
    assertTrue(id.startsWith(endingId.substring(0, 2)));
  }

  // Tests we can re-compute the date from the UUID.
  @Test
  public void testComputeDate() throws Exception {
    GuidGeneratorWithTime guid = new GuidGeneratorWithTime();
    Date date = computeTime(guid.getUUID());
    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_DAY);
    assertEquals(today(), format.format(date));
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_WITH_TIME, Locale.US);
    Date startOfToday = formatter.parse(today() + START_OF_DAY);
    Date endOfToday = formatter.parse(today() + END_OF_DAY);
    assertTrue(inRange(guid.getUUID(), startOfToday, endOfToday));
  }

  private static String today() {
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_DAY);
    return df.format(new Date());
  }

  private static String generateRangeBoundary(boolean startOfDay) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_WITH_TIME, Locale.US);
    byte[] transactionId = new byte[UUID_LENGTH];
    Date date = formatter.parse(today() + (startOfDay ? START_OF_DAY : END_OF_DAY));
    byte[] timeComponent = someBytes(() -> {
      final ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
      buf.putLong(date.getTime());
      return buf.array();
    });
    final byte[] randomComponent = new byte[RANDOM_BYTE_LENGTH];
    Arrays.fill(randomComponent, (byte) 0x00);
    System.arraycopy(timeComponent, TIME_SIGNIFICANT_BYTES_OFFSET, transactionId, 0,
        TIME_BYTE_LENGTH);
    System.arraycopy(randomComponent, 0, transactionId, TIME_BYTE_LENGTH, RANDOM_BYTE_LENGTH);
    return Base58.encode(transactionId);
  }

  private static boolean inRange(String id, Date startDate, Date endDate) {
    Instant uuid = computeTime(id).toInstant();
    Instant start = startDate.toInstant();
    Instant end = endDate.toInstant();
    return !(uuid.isBefore(start) || uuid.isAfter(end));
  }


}
