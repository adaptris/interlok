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

import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

public class JdbcMapInsertTest extends JdbcMapInsertCase {

  @Test
  public void testService() throws Exception {
    createDatabase();
    InsertProperties service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsInserted");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ServiceCase.execute(service, msg);
    doAssert(1);
    assertTrue(msg.headersContainsKey("rowsInserted"));
    assertEquals("1", msg.getMetadataValue("rowsInserted"));
  }

  @Test
  public void testService_WithBookend() throws Exception {
    createDatabase(CREATE_QUOTED);
    InsertProperties service = configureForTests(createService());
    service.withColumnBookend('"');
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ServiceCase.execute(service, msg);
    doAssert(1);
  }

  @Test
  public void testService_NoConverters() throws Exception {
    createDatabase();
    InsertProperties service = configureForTests(createService());
    service.setFieldMappings(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ServiceCase.execute(service, msg);
    doAssert(1);
  }

  @Test
  public void testService_InvalidConversion() throws Exception {
    createDatabase();
    KeyValuePairSet mappings = new KeyValuePairSet();
    mappings.add(new KeyValuePair("dob", "com.adaptris.does.not.Exist"));
    mappings.add(new KeyValuePair("firstname", "java.lang.String"));
    // mappings.add(new KeyValuePair("lastname", "Date"));
    InsertProperties service = configureForTests(createService());
    service.setFieldMappings(mappings);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CONTENT);
    ServiceCase.execute(service, msg);
    doAssert(1);
  }

  @Test
  public void testService_InvalidColumns() throws Exception {
    createDatabase();
    InsertProperties service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMN);
    try {
      ServiceCase.execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected InsertProperties createService() {
    return new InsertProperties();
  }

  protected class InsertProperties extends JdbcMapInsert {

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      Connection conn = null;
      try {
        conn = getConnection(msg);
        addUpdatedMetadata(handleInsert(table(msg), conn, mapify(msg)), msg);
        JdbcUtil.commit(conn, msg);
      }
      catch (Exception e) {
        JdbcUtil.rollback(conn, msg);
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
