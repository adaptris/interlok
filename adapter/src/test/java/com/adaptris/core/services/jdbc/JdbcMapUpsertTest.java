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
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

public class JdbcMapUpsertTest extends JdbcMapInsertCase {

  protected static final String UTC_0 = "1970-01-01";
  protected static final String SMITH = "smith";
  protected static final String ID_ELEMENT_VALUE = "firstname";
  protected static final String ALICE = "alice";
  protected static final String DOB = "2017-01-01";

  @Test
  public void testDatabaseId() {
    JdbcMapUpsert upsert = (JdbcMapUpsert) createService();
    assertEquals(JdbcMapUpsert.DEFAULT_ID_FIELD, upsert.idField());
    assertEquals("hello", upsert.withId("hello").idField());
  }

  @Test
  public void testService_Insert() throws Exception {
    createDatabase();
    UpsertProperties service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ServiceCase.execute(service, msg);
    doAssert(1);
  }

  @Test
  public void testService_InvalidColumns() throws Exception {
    createDatabase();
    JdbcMapUpsert service = configureForTests(createService()).withId(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN);
    try {
      ServiceCase.execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_Update() throws Exception {
    createDatabase();
    populateDatabase();
    UpsertProperties service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ServiceCase.execute(service, msg);
    doAssert(1);
    checkDob(ALICE, DOB);
  }

  protected UpsertProperties createService() {
    return new UpsertProperties();
  }

  protected static void populateDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      s.execute(
          String.format("INSERT INTO %s (firstname, lastname, dob) VALUES ('%s', '%s' ,'%s')", TABLE_NAME, ALICE, SMITH,
          UTC_0));
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected static void checkDob(String firstname, String dob) throws Exception {
    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = createConnection();
      s = c.createStatement();
      rs = s.executeQuery(String.format("SELECT * FROM %s WHERE firstname='%s'", TABLE_NAME, firstname));
      if (rs.next()) {
        assertEquals(dob, rs.getString("dob"));
      } else {
        fail("No Match for firstname: " + firstname);
      }
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected class UpsertProperties extends JdbcMapUpsert {

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      Connection conn = null;
      try {
        conn = getConnection(msg);
        handleUpsert(conn, mapify(msg));
        commit(conn, msg);
      }
      catch (Exception e) {
        rollback(conn, msg);
        throw ExceptionHelper.wrapServiceException(e);
      }
      finally {
        JdbcUtil.closeQuietly(conn);
      }
    }

    protected Map<String, String> mapify(AdaptrisMessage msg) throws Exception {
      Properties result = new Properties();
      try (InputStream in = msg.getInputStream()) {
        result.load(in);
      }
      return KeyValuePairBag.asMap(new KeyValuePairSet(result));
    }
  }
}
