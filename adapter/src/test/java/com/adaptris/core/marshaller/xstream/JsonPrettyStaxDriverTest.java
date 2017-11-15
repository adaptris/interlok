/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.marshaller.xstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.json.JettisonStaxWriter;
import com.thoughtworks.xstream.io.xml.StaxWriter;

public class JsonPrettyStaxDriverTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreateWriter_SerializeAsArray() throws Exception {
    JsonPrettyStaxDriver d = new JsonPrettyStaxDriver();
    assertEquals(JettisonStaxWriter.class, d.createWriter(new StringWriter()).getClass());
    assertEquals(JettisonStaxWriter.class, d.createWriter(new ByteArrayOutputStream()).getClass());
  }

  @Test
  public void testCreateWriter_SerializeAsArrayFalse() throws Exception {
    JsonPrettyStaxDriver d = new JsonPrettyStaxDriver(new Configuration(), false);
    assertEquals(StaxWriter.class, d.createWriter(new StringWriter()).getClass());
    assertEquals(StaxWriter.class, d.createWriter(new ByteArrayOutputStream()).getClass());
  }

  @Test
  public void testCreateWriter_Exception() throws Exception {
    JsonPrettyStaxDriver d = new JsonPrettyStaxDriver(new Configuration(), false);
    MappedXMLOutputFactory mock = Mockito.mock(MappedXMLOutputFactory.class);
    d.myOutputFactory = mock;
    Mockito.doThrow(new XMLStreamException()).when(mock).createXMLStreamWriter(Matchers.any(Writer.class));
    Mockito.doThrow(new XMLStreamException()).when(mock).createXMLStreamWriter(Matchers.any(OutputStream.class));
    try {
      d.createWriter(new StringWriter());
      fail();
    } catch (StreamException expected) {
      
    }
    try {
      d.createWriter(new ByteArrayOutputStream());
      fail();
    } catch (StreamException expected) {
      
    }
  }

  @Test
  public void testPrettyStaxDriver_CreateWriter() {
    PrettyStaxDriver d = new PrettyStaxDriver();
    assertNotNull(d.createWriter(new ByteArrayOutputStream()));
  }
}
