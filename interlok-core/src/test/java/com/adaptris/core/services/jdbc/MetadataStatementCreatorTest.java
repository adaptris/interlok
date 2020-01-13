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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;

public class MetadataStatementCreatorTest extends JdbcQueryServiceCase {

  protected static final String QUERY_SQL_2 =
      "SELECT adapter_version"
      + " FROM adapter_type_version " + " WHERE adapter_unique_id = ?";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testMetadataStatementCreator() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    String sql = ((ConfiguredSQLStatement) s.getStatementCreator()).getStatement();
    
    MetadataSQLStatement ms = new MetadataSQLStatement();
    ms.setMetadataKey("sqlStatement");
    s.setStatementCreator(ms);

    // Copy the statement from the service to the metadata of the message to test the metadatastamentcreator
    msg.addMetadata("sqlStatement", sql);
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getContent());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }
  
  @Test
  public void testMultipleExecutions() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry1 = dbItems.get(0);
    AdapterTypeVersion entry2 = dbItems.get(1);

    String metadataQuery1 = QUERY_SQL;
    String metadataQuery2 = QUERY_SQL_2;

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg1 = createMessage(entry1);
    msg1.addMetadata("sqlStatement", metadataQuery1);
    AdaptrisMessage msg2 = createMessage(entry2);
    msg2.addMetadata("sqlStatement", metadataQuery2);

    MetadataSQLStatement ms = new MetadataSQLStatement();
    ms.setMetadataKey("sqlStatement");
    s.setStatementCreator(ms);
    try {
      start(s);
      s.doService(msg1);
      assertTrue(msg1.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
      assertTrue(msg1.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
      assertEquals(entry1.getVersion(), msg1.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
      assertEquals(entry1.getTranslatorType(), msg1.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));

      s.doService(msg2);

      assertTrue(msg2.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
      assertFalse(msg2.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
      assertEquals(entry2.getVersion(), msg2.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));

    } finally {
      stop(s);
    }
  }


  @Override
  protected FirstRowMetadataTranslator createTranslatorForConfig() {
    return new FirstRowMetadataTranslator();
  }


}
