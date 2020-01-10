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

package com.adaptris.core.services.metadata.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import com.adaptris.core.CoreException;

public abstract class XpathQueryCase {

  private static Log log = LogFactory.getLog(XpathQueryCase.class);

  public static final String XML = "<?xml version=\"1.0\"?><message><message-type>order"
      + "</message-type><source-id>partnera</source-id><destination-id>" + "partnerb</destination-id><body>...</body>"
      + "<extra att=\"single\">one</extra><extra att=\"multi\">two</extra>" + "<extra att=\"multi\">three</extra></message>";

  public static final String XML_WITH_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
      + "<svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:sch=\"http://www.ascc.net/xml/schematron\" xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" xmlns:dp=\"http://www.dpawson.co.uk/ns#\" title=\"Anglia Farmers AF xml Invoice Schematron File\" schemaVersion=\"ISO19757-3\">\n"
      + "<svrl:ns-prefix-in-attribute-values uri=\"http://www.dpawson.co.uk/ns#\" prefix=\"dp\"/>\n" + "<svrl:active-pattern/>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData\"/>\n"
      + "<svrl:failed-assert test=\"STOCK_CODE != ''\" location=\"/SageData[1]/JoinedData[1]\">\n"
      + "<svrl:text>Error: Product Code must be present.</svrl:text>\n" + "</svrl:failed-assert>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData/CUST_ORDER_NUMBER[. != '']\"/>\n"
      + "<svrl:fired-rule context=\"SageData/JoinedData/AF_NUMBER[. != '']\"/>\n"
      + "<svrl:failed-assert test=\"string-length(.) = 5\" location=\"/SageData[1]/JoinedData[1]/AF_NUMBER[1]\">\n"
      + "<svrl:text>Error: Anglia Farmer's Supplier Number must be 5 digits long. (Current Value: 62826123)</svrl:text>\n"
      + "</svrl:failed-assert>\n" + "</svrl:schematron-output>";

  protected abstract XpathObjectQuery create();

  @Test
  public void testSetMetadataKey() throws Exception {
    XpathQueryImpl query = (XpathQueryImpl) create();
    assertNull(query.getMetadataKey());
    query.setMetadataKey("key");
    assertEquals("key", query.getMetadataKey());
    try {
      query.setMetadataKey("");
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("key", query.getMetadataKey());
    try {
      query.setMetadataKey(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals("key", query.getMetadataKey());
  }

  @Test
  public void testSetAllowNullResults() throws Exception {
    XpathQueryImpl query = (XpathQueryImpl) create();
    assertNull(query.getAllowEmptyResults());
    assertFalse(query.allowEmptyResults());
    query.setAllowEmptyResults(Boolean.FALSE);
    assertNotNull(query.getAllowEmptyResults());
    assertEquals(Boolean.FALSE, query.getAllowEmptyResults());
    assertFalse(query.allowEmptyResults());
    query.setAllowEmptyResults(null);
    assertFalse(query.allowEmptyResults());
    assertNull(query.getAllowEmptyResults());
  }

  @Test
  public void testInit_NoMetadataKey() throws Exception {
    XpathQueryImpl query = (XpathQueryImpl) create();
    try {
      query.verify();
      fail();
    }
    catch (CoreException expected) {

    }
  }

  protected static class StaticNamespaceContext implements NamespaceContext {

    private final Map<String, String> prefixMap;
    private final Map<String, Set<String>> nsMap;

    public StaticNamespaceContext() {
      Map<String, String> map = new HashMap<String, String>();
      map.put("svrl", "http://purl.oclc.org/dsdl/svrl");
      map.put("xsd", "http://www.w3.org/2001/XMLSchema");
      map.put("xs", "http://www.w3.org/2001/XMLSchema");
      map.put("sch", "http://www.ascc.net/xml/schematron");
      map.put("iso", "http://purl.oclc.org/dsdl/schematron");
      map.put("dp", "http://www.dpawson.co.uk/ns#");
      prefixMap = createPrefixMap(map);
      nsMap = createNamespaceMap(prefixMap);
    }

    public StaticNamespaceContext(Map<String, String> prefixMappings) {
      prefixMap = createPrefixMap(prefixMappings);
      nsMap = createNamespaceMap(prefixMap);
    }


    private Map<String, String> createPrefixMap(Map<String, String> source) {
      Map<String, String> result = new HashMap<String, String>(source);
      addDefault(result, XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
      addDefault(result, XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
      return Collections.unmodifiableMap(result);
    }

    private void addDefault(Map<String, String> prefixMap, String prefix, String nsURI) {
      String previous = prefixMap.put(prefix, nsURI);
      if (previous != null && !previous.equals(nsURI)) {
        throw new IllegalArgumentException("Couldn't add default prefixes");
      }
    }

    private Map<String, Set<String>> createNamespaceMap(Map<String, String> prefixMap) {
      Map<String, Set<String>> result = new HashMap<String, Set<String>>();
      for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
        String nsURI = entry.getValue();
        Set<String> existingPrefixes = result.get(nsURI);
        if (isNull(existingPrefixes)) {
          existingPrefixes = new HashSet<String>();
          result.put(nsURI, existingPrefixes);
        }
        existingPrefixes.add(entry.getKey());
      }
      for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
        Set<String> readOnly = Collections.unmodifiableSet(entry.getValue());
        entry.setValue(readOnly);
      }
      return result;
    }

    @Override
    public String getNamespaceURI(String prefix) {
      if (isNull(prefix)) {
        return XMLConstants.NULL_NS_URI;
      }
      String nsURI = prefixMap.get(prefix);
      return isNull(nsURI) ? XMLConstants.NULL_NS_URI : nsURI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
      if (isNull(namespaceURI)) {
        return null;
      }
      Set<String> prefixes = nsMap.get(namespaceURI);
      return isNull(prefixes) ? null : prefixes.iterator().next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      if (isNull(namespaceURI)) {
        return null;
      }
      Set<String> prefixes = nsMap.get(namespaceURI);
      return isNull(prefixes) ? null : prefixes.iterator();
    }

    private boolean isNull(Object value) {
      return null == value;
    }
  }
}
