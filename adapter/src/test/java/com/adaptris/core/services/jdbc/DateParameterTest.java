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

package com.adaptris.core.services.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.ServiceException;

public class DateParameterTest {

  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private String dateString;
  private java.sql.Date date;

  @Before
  public void setUp() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    dateString = sdf.format(new java.util.Date());
    date = new java.sql.Date(sdf.parse(dateString).getTime());
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testConvert() throws Exception {
    assertEquals(date, create().toDate(dateString));
  }

  @Test
  public void testUnparseableFormat() throws Exception {
    DateStatementParameter sp = create();
    sp.setDateFormat("yyyy-MM-ddHH:mm:ss");
    try {
      sp.toDate(dateString);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertNull() throws Exception {
    DateStatementParameter sp = create();
    sp.setConvertNull(false);
    try {
      sp.toDate(null);
      fail("Expected ServiceException");
    }
    catch (ServiceException expected) {
      // expected
    }
  }

  @Test
  public void testConvertWithConvertNull() throws Exception {
    DateStatementParameter sp = create();
    sp.setConvertNull(true);
    long convertedTime = ((java.sql.Date) sp.toDate(null)).getTime();
    long now = System.currentTimeMillis();
    assertTrue("now > convertedTime", now >= convertedTime);
  }

  private DateStatementParameter create() throws Exception {
    DateStatementParameter sp = new DateStatementParameter();
    sp.setDateFormat(DATE_FORMAT);
    return sp;
  }
}
