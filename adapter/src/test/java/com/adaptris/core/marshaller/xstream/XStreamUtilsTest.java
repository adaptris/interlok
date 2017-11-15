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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.WorkflowImp;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.XpathMetadataService;
import com.adaptris.core.services.metadata.xpath.ConfiguredXpathQuery;
import com.adaptris.util.stream.StreamUtilTest;
import com.thoughtworks.xstream.core.util.FastField;

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
    try (InputStream in = IOUtils.toInputStream(sw.toString())) {
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

  @Test
  public void testGetClassFieldByType() {
    Set<Field> classFieldByType = XStreamUtils.getClassFieldByType(WorkflowImp.class, ServiceCollection.class);
    assertEquals(1, classFieldByType.size());
    for (Iterator<Field> iterator = classFieldByType.iterator(); iterator.hasNext();) {
      Field field = (Field) iterator.next();
      assertEquals("serviceCollection", field.getName());
      break;
    }
    
    classFieldByType = XStreamUtils.getClassFieldByType(WorkflowImp.class, AdaptrisMessageConsumer.class);
    assertEquals(1, classFieldByType.size());
    for (Iterator<Field> iterator = classFieldByType.iterator(); iterator.hasNext();) {
      Field field = (Field) iterator.next();
      assertEquals("consumer", field.getName());
      break;
    }
    
    classFieldByType = XStreamUtils.getClassFieldByType(WorkflowImp.class, String.class);
    assertEquals(1, classFieldByType.size());
    for (Iterator<Field> iterator = classFieldByType.iterator(); iterator.hasNext();) {
      Field field = (Field) iterator.next();
      assertEquals("uniqueId", field.getName());
      break;
    }
    
    classFieldByType = XStreamUtils.getClassFieldByType(WorkflowImp.class, Boolean.class);
    assertEquals(3, classFieldByType.size());
    int counter = 0;
    for (Iterator<Field> iterator = classFieldByType.iterator(); iterator.hasNext();) {
      Field field = (Field) iterator.next();
      if (counter == 0) assertEquals("sendEvents", field.getName());
      else if (counter == 1) assertEquals("logPayload", field.getName());
      else if (counter == 2) assertEquals("disableDefaultMessageCount", field.getName());
      break;
    }
  }

  @Test
  public void testGetMatchedFieldFromClass() {
    Set<FastField> seenProperties = new HashSet<>();
    Class<?> parentClass = WorkflowImp.class;
    // Get the first boolean field
    Field matchedFieldFromClass = XStreamUtils.getMatchedFieldFromClass(parentClass, Boolean.class, seenProperties);
    assertEquals("sendEvents", matchedFieldFromClass.getName());
    
    // Now register the field as processed so as to ensure that it is not returned again
    FastField fastField = new FastField(parentClass, "sendEvents");
    seenProperties.add(fastField);
    matchedFieldFromClass = XStreamUtils.getMatchedFieldFromClass(parentClass, Boolean.class, seenProperties);
    assertEquals("logPayload", matchedFieldFromClass.getName());
  }

//  public void testGetGenericTypesForField() {
//    List<Field> fields = new ArrayList<>();
//    fields = XStreamUtils.getFieldsForClassEnsuringUniqueFieldNames(WorkflowImp.class, fields);
//    String fieldName = null;
//    int assertionCount = 0;
//    List<Class<?>> genericsClassTypesForClass = null;
//    for (Field f : fields) {
//      fieldName = f.getName();
//      switch(fieldName) {
//      case "serviceCollection" :
//        genericsClassTypesForClass = XStreamUtils.getGenericTypesForField(f);
//        assertNotNull(genericsClassTypesForClass);
//        assertEquals(0, genericsClassTypesForClass.size());
//        assertionCount++;
//        break;
//        
//      case "interceptors" :
//        genericsClassTypesForClass = XStreamUtils.getGenericTypesForField(f);
//        assertNotNull(genericsClassTypesForClass);
//        assertEquals(1, genericsClassTypesForClass.size());
//        assertTrue(genericsClassTypesForClass.contains(WorkflowInterceptor.class));
//        assertionCount++;
//      }
//    }
//    assertEquals(2, assertionCount); // This ensures that we processed the interceptors field, just in case someone renamed it
//  }

  @Test
  public void testGetGenericHierarchicalTypesForField() {
    List<Field> fields = new ArrayList<>();
    fields = XStreamUtils.getFieldsForClassEnsuringUniqueFieldNames(WorkflowImp.class, fields);
    String fieldName = null;
    int assertionCount = 0;
    Set<Class<?>> genericsClassTypesForClass = null;
    for (Field f : fields) {
      fieldName = f.getName();
      switch(fieldName) {
      case "serviceCollection" :
        genericsClassTypesForClass = XStreamUtils.getGenericHierarchicalTypesForField(f);
        assertNotNull(genericsClassTypesForClass);
        assertEquals(2, genericsClassTypesForClass.size());
        assertTrue(genericsClassTypesForClass.contains(Service.class));
        assertTrue(genericsClassTypesForClass.contains(EventHandlerAware.class));
        assertionCount++;
        break;
        
      case "interceptors" :
        genericsClassTypesForClass = XStreamUtils.getGenericHierarchicalTypesForField(f);
        assertNotNull(genericsClassTypesForClass);
        assertEquals(1, genericsClassTypesForClass.size());
        assertTrue(genericsClassTypesForClass.contains(WorkflowInterceptor.class));
        assertionCount++;
      }
    }
    assertEquals(2, assertionCount); // This ensures that we processed the interceptors field, just in case someone renamed it
  }

  @Test
  public void testGetGenericHierarchicalTypesForClass() {
    Set<Class<?>> genericTypeClassesSet = new HashSet<>(); 
    Set<Class<?>> resultClassSet = XStreamUtils.getGenericHierarchicalTypesForClass(ServiceCollection.class, genericTypeClassesSet);
    assertNotNull(resultClassSet);
    assertEquals(2, resultClassSet.size());
    assertTrue(resultClassSet.contains(Service.class));
    assertTrue(resultClassSet.contains(com.adaptris.core.EventHandlerAware.class));
    
    resultClassSet.clear();
    resultClassSet = XStreamUtils.getGenericHierarchicalTypesForClass(ServiceList.class, genericTypeClassesSet);
    assertNotNull(resultClassSet);
    assertEquals(1, resultClassSet.size());
    assertTrue(resultClassSet.contains(Service.class));
    
    resultClassSet.clear();
    resultClassSet = XStreamUtils.getGenericHierarchicalTypesForClass(ServiceImp.class, genericTypeClassesSet);
    assertNotNull(resultClassSet);
    assertEquals(1, resultClassSet.size());
    assertTrue(resultClassSet.contains(Service.class));
  }

  @Test
  public void testGetImplicitCollectionFieldNameForType() {
    // Main test scenario
    String fieldNameForType = XStreamUtils.getImplicitCollectionFieldNameForType(XpathMetadataService.class, ConfiguredXpathQuery.class);
    assertEquals("xpathQueries", fieldNameForType);

    fieldNameForType = XStreamUtils.getImplicitCollectionFieldNameForType(WorkflowImp.class, AdaptrisMessageConsumer.class);
    assertNull(fieldNameForType);

    fieldNameForType = XStreamUtils.getImplicitCollectionFieldNameForType(AddMetadataService.class, String.class);
    assertNull(fieldNameForType);
  }
}
