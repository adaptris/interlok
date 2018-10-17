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

package com.adaptris.core.marshaller.xstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.ServiceList;
import com.adaptris.core.WorkflowImp;
import com.adaptris.util.stream.StreamUtilTest;

public class XStreamUtilsTest extends XStreamUtils {

  @Test
  public void testToFieldName() {
    assertNull(XStreamUtils.toFieldName(null));
    assertEquals("", XStreamUtils.toFieldName(""));
    assertEquals(" ", XStreamUtils.toFieldName(" "));
    assertEquals("   ", XStreamUtils.toFieldName("   "));
    assertEquals("a", XStreamUtils.toFieldName("a"));
    assertEquals("1", XStreamUtils.toFieldName("1"));
    assertEquals("aDog", XStreamUtils.toFieldName("a-dog"));
    assertEquals("aSimpleXmlTagname", XStreamUtils.toFieldName("a-simple-xml-tagname"));
    assertEquals("a.simple.xml.tagname", XStreamUtils.toFieldName("a.simple.xml.tagname"));
    assertEquals("AB", XStreamUtils.toFieldName("AB"));
    assertEquals("ABCd", XStreamUtils.toFieldName("ABCd"));
    assertEquals("ABC-d", XStreamUtils.toFieldName("ABC-d"));
    assertEquals("abc", XStreamUtils.toFieldName("Abc"));
    assertEquals("aSimpleXmlTagname", XStreamUtils.toFieldName("a-simple-xml--tagname"));

  }

  @Test
  public void testToXmlElementName() {
    assertNull("", XStreamUtils.toXmlElementName(null));
    assertEquals("", XStreamUtils.toXmlElementName(""));
    assertEquals("m", XStreamUtils.toXmlElementName("m"));
    assertEquals("m", XStreamUtils.toXmlElementName("M"));
    assertEquals("MM", XStreamUtils.toXmlElementName("MM"));
    assertEquals("mm", XStreamUtils.toXmlElementName("Mm"));
    assertEquals("MMUNTOUCHEDandAbitMore", XStreamUtils.toXmlElementName("MMUNTOUCHEDandAbitMore"));
    assertEquals("my-class", XStreamUtils.toXmlElementName("myClass"));

    assertEquals("the-little-dog-saw-aCat", XStreamUtils.toXmlElementName("theLittleDogSawACat"));
    assertEquals("the-little-dog-saw-arat", XStreamUtils.toXmlElementName("theLittleDogSawArat"));
    // we shouldn't give it a fully qualified classname, but this is what happens
    assertEquals("java.lang.-string", XStreamUtils.toXmlElementName("java.lang.String"));
    assertEquals("com.adaptris.core.http.jetty.-jetty-pooling-workflow-interceptor", XStreamUtils.toXmlElementName("com.adaptris.core.http.jetty.JettyPoolingWorkflowInterceptor"));

  }

  @Test
  public void testGetClasses() throws Exception {
    StringWriter sw = new StringWriter();
    try (PrintWriter pw = new PrintWriter(sw)) {
      pw.println(Adapter.class.getName());
      pw.println(ServiceList.class.getName());
      pw.println("hello.world");
    }
    try (InputStream in = IOUtils.toInputStream(sw.toString(), Charset.defaultCharset())) {
      List<Class<?>> classes = getClasses(in);
      assertEquals(2, classes.size());
    }
  }

  @Test(expected = IOException.class)
  public void testGetClasses_Exception() throws Exception {
    try (InputStream in = new StreamUtilTest.ErroringInputStream()) {
      List<Class<?>> classes = getClasses(in);
    }
  }

  @Test
  public void testCreateParentFields() {
    Collection<String> resultCollection = XStreamUtils.createParentFields(WorkflowImp.class, "serviceCollection", ".");
    assertTrue(resultCollection.contains("com.adaptris.core.WorkflowImp.serviceCollection"));
    resultCollection = XStreamUtils.createParentFields(WorkflowImp.class, "serviceCollection", "-");
    assertTrue(resultCollection.contains("com.adaptris.core.WorkflowImp-serviceCollection"));
  }

  @Test
  public void testSetContainsAnyOf() {
    Set<String> testSet = new HashSet<>();
    testSet.add("cat");
    testSet.add("dog");
    testSet.add("fish");
    Collection<String> searchCollection = new HashSet<String>();
    searchCollection.add("horse");
    assertFalse(XStreamUtils.setContainsAnyOf(testSet, searchCollection));
    searchCollection.add("cat");
    assertTrue(XStreamUtils.setContainsAnyOf(testSet, searchCollection));
    searchCollection.clear();
    searchCollection.add("fish");
    assertTrue(XStreamUtils.setContainsAnyOf(testSet, searchCollection));
  }

}
