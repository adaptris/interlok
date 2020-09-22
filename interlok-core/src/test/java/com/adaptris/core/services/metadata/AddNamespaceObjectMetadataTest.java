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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import javax.xml.namespace.NamespaceContext;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;

public class AddNamespaceObjectMetadataTest extends MetadataServiceExample {


  @Test
  public void testService() throws Exception {
    AddNamespaceObjectMetadata service = new AddNamespaceObjectMetadata(createContextEntries());
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      execute(service, msg);
      assertTrue(msg.getObjectHeaders().containsKey(SimpleNamespaceContext.class.getCanonicalName()));
      NamespaceContext ctx = (NamespaceContext) msg.getObjectHeaders().get(SimpleNamespaceContext.class.getCanonicalName());
      // Configured NamespaceContext takes precendence.
      assertNotSame(ctx, SimpleNamespaceContext.create(createContextEntries(), msg));
      // If we have nothing configured, it should now be the same.
      assertEquals(ctx, SimpleNamespaceContext.create(null, msg));
    }
    finally {

    }
  }

  @Test
  public void testService_NoNamespaceContext() throws Exception {
    AddNamespaceObjectMetadata service = new AddNamespaceObjectMetadata();
    try {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      execute(service, msg);
      assertFalse(msg.getObjectHeaders().containsKey(SimpleNamespaceContext.class.getCanonicalName()));
      assertNotNull(SimpleNamespaceContext.create(createContextEntries(), msg));
    }
    finally {

    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new AddNamespaceObjectMetadata(createContextEntries());
  }

  private KeyValuePairSet createContextEntries() {
    KeyValuePairSet contextEntries = new KeyValuePairSet();
    contextEntries.add(new KeyValuePair("svrl", "http://purl.oclc.org/dsdl/svrl"));
    contextEntries.add(new KeyValuePair("xsd", "http://www.w3.org/2001/XMLSchema"));
    contextEntries.add(new KeyValuePair("xs", "http://www.w3.org/2001/XMLSchema"));
    contextEntries.add(new KeyValuePair("sch", "http://www.ascc.net/xml/schematron"));
    contextEntries.add(new KeyValuePair("iso", "http://purl.oclc.org/dsdl/schematron"));
    contextEntries.add(new KeyValuePair("dp", "http://www.dpawson.co.uk/ns#"));
    return contextEntries;
  }
}
