/*
 * Copyright 2019 Adaptris Ltd.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.adaptris.annotation.AnnotationConstants;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class PrettyStaxDriverTest {

  private static HashSet<String> cdata = new HashSet<>();

  @BeforeClass
  public static void setUp() throws Exception {
    cdata = loadCDATA();
  }

  @Test
  public void testCreateReader() {
    PrettyStaxDriver driver = new PrettyStaxDriver(cdata, true);
    assertNotNull(driver.createReader(new ByteArrayInputStream("<xml/>".getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void testCreateWriter() {
    PrettyStaxDriver driver = new PrettyStaxDriver(cdata, true);
    assertNotNull(driver.createWriter(new ByteArrayOutputStream()));
  }

  @Test
  public void testXmlInputFactory() {
    assertNotNull(new PrettyStaxDriver(cdata, true).createInputFactory());
    assertNotNull(new PrettyStaxDriver(cdata, false).createInputFactory());
  }

  @Test
  public void testXmlOutputFactory() {
    assertNotNull(new PrettyStaxDriver(cdata, true).createOutputFactory());
    assertNotNull(new PrettyStaxDriver(cdata, false).createOutputFactory());
  }

  @Test
  public void testPrettyPrintWriter() throws Exception {
    PrettyStaxDriver driver = new PrettyStaxDriver(cdata, true);
    // service
    //      <payload-from-metadata-service>
    //        <unique-id>naughty-hodgkin</unique-id>
    //        <template><![CDATA[Hello World]]></template>
    //      </payload-from-metadata-service>
    StringWriter writer = new StringWriter();
    try (Writer w = writer) {
      PrettyPrintWriter printWriter = (PrettyPrintWriter) driver.createWriter(w);
      printWriter.startNode("payload-from-metadata-service", PayloadFromMetadataService.class);
      printWriter.startNode("unique-id", String.class);
      printWriter.setValue("naughty-hodgkin");
      printWriter.endNode(); // unique-id
      printWriter.startNode("template", String.class);
      printWriter.setValue("Hello World");
      printWriter.endNode(); // template
      printWriter.endNode(); // payload-from-metadata-service
    }
    System.err.println(writer.toString());
    assertTrue(writer.toString().contains("CDATA"));
  }


  private static HashSet<String> loadCDATA() throws Exception {
    HashSet<String> result = new HashSet<>();
    Enumeration<URL> mappings = PrettyStaxDriverTest.class.getClassLoader().getResources(AnnotationConstants.CDATA_PROPERTIES_FILE);
    while (mappings.hasMoreElements()) {
      try (InputStream in = mappings.nextElement().openStream()) {
        result.addAll(XStreamUtils.readResource(in));
      }
    }
    return result;
  }
}
