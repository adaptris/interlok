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

import java.util.List;

import com.adaptris.core.AdaptrisMessage;

public class MetadataStatementCreatorTest extends JdbcQueryServiceCase {

  public MetadataStatementCreatorTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {

  }

  public void testMetadataStatementCreator() throws Exception {
    createDatabase();
    List<AdapterTypeVersion> dbItems = generate(10);
    AdapterTypeVersion entry = dbItems.get(0);

    populateDatabase(dbItems, false);
    JdbcDataQueryService s = createMetadataService();
    FirstRowMetadataTranslator t = new FirstRowMetadataTranslator();
    s.setResultSetTranslator(t);
    AdaptrisMessage msg = createMessage(entry);
    
    MetadataSQLStatement ms = new MetadataSQLStatement();
    ms.setMetadataKey("sqlStatement");
    s.setStatementCreator(ms);

    // Copy the statement from the service to the metadata of the message to test the metadatastamentcreator
    msg.addMetadata("sqlStatement", s.getStatement());
    s.setStatement(null); // Get rid of the statement in the service
    assertNull(s.getStatement()); // Make sure the service doesn't have the statement anymore
    
    execute(s, msg);
    assertEquals(XML_PAYLOAD_PREFIX + entry.getUniqueId() + XML_PAYLOAD_SUFFIX, msg.getStringPayload());
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertEquals(entry.getVersion(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_VERSION));
    assertTrue(msg.containsKey(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertEquals(entry.getTranslatorType(), msg.getMetadataValue(t.getMetadataKeyPrefix() + t.getSeparator() + COLUMN_TYPE));
    assertFalse(msg.containsKey(JdbcDataQueryService.class.getCanonicalName()));
  }
  
  @Override
  protected FirstRowMetadataTranslator createTranslatorForConfig() {
    return new FirstRowMetadataTranslator();
  }


}
