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

package com.adaptris.core.jdbc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.util.GuidGenerator;

public abstract class JdbcServiceCase extends ServiceCase {

  private static final GuidGenerator GUID = new GuidGenerator();


  protected <T extends JdbcService> void testBackReferences(T testObject) throws Exception {
    JdbcConnection conn = new JdbcConnection();
    conn.setConnectUrl("jdbc:derby:memory:" + GUID.safeUUID() + ";create=true");
    conn.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");
    testObject.setConnection(conn);
    testObject.init(); //redmineID #4536, exception listener is set in .init()
    assertEquals(conn, testObject.getConnection());
    assertEquals(1, conn.retrieveExceptionListeners().size());
    assertTrue(testObject == conn.retrieveExceptionListeners().toArray()[0]);

    testObject.start();
    testObject.stop();
    testObject.close();
    
    // Now marshall and see if it's the same.
    XStreamMarshaller m = new XStreamMarshaller();
    String xml = m.marshal(testObject);
    @SuppressWarnings("unchecked")
    T testObject2 = (T) m.unmarshal(xml);
    testObject2.init(); //redmineID #4536, exception listener now must simply be set in .init()
    // If the setter has been used, then these two will be "true"
    assertNotNull(testObject2.getConnection());
    assertEquals(1, testObject2.getConnection().retrieveExceptionListeners().size());
    assertTrue(testObject2 == testObject2.getConnection().retrieveExceptionListeners().toArray()[0]);
    
    testObject2.start();
    testObject2.stop();
    testObject2.close();    
    
  }

}
