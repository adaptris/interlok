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
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.services.jdbc.types.StringColumnTranslator;

public class SimplePayloadTranslatorTest extends JdbcQueryServiceCase {

  private static final String METADATA_KEY_DATE = "date";
  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String DATE_QUERY_SQL = "SELECT adapter_version, message_translator_type, inserted_on, counter"
      + " FROM adapter_type_version " + " WHERE inserted_on > ?";


  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  @Test
  public void testService_NamedColumn() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    SimplePayloadResultSetTranslator t = new SimplePayloadResultSetTranslator();
    t.setColumnName("MESSAGE_TRANSLATOR_TYPE");
    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(msg.getContent(), entry.getTranslatorType());
  }

  // "SELECT adapter_version, message_translator_type, inserted_on, counter FROM adapter_type_version "
  @Test
  public void testService_NoResultSet() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    SimplePayloadResultSetTranslator t = new SimplePayloadResultSetTranslator();
    t.setColumnName("MESSAGE_TRANSLATOR_TYPE");
    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    msg.addMetadata(ADAPTER_ID_KEY, getName());
    execute(s, msg);
    assertEquals("", msg.getContent());
  }

  @Test
  public void testService_NoColumn() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    SimplePayloadResultSetTranslator t = new SimplePayloadResultSetTranslator();
    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(msg.getContent(), entry.getVersion());
  }

  @Test
  public void testService_NamedColumn_ColumnTranslator() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    SimplePayloadResultSetTranslator t = new SimplePayloadResultSetTranslator();
    t.setColumnWriter(new StringColumnTranslator());
    t.setColumnName("MESSAGE_TRANSLATOR_TYPE");
    s.setResultSetTranslator(t);

    AdaptrisMessage msg = createMessage(entry);
    execute(s, msg);
    assertEquals(msg.getContent(), entry.getTranslatorType());
  }

  @Override
  protected SimplePayloadResultSetTranslator createTranslatorForConfig() {
    return new SimplePayloadResultSetTranslator();
  }

}
