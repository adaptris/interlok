package com.adaptris.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class XpathBuilderTest {

  public static final String XML_NO_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
      + "<root>\n"
      + "   <xpath1>xpath&amp;1_result</xpath1>\n"
      + "   <xpath2>&lt;xpath2_result</xpath2>\n"
      + "   <xpath3>xpath3_result</xpath3>\n"
      + "   <xpath3>xpath3_result</xpath3>\n"
      + "   <parent>\n"
      + "      <child1>child1_result</child1>\n"
      + "      <child2>child2_result</child2>\n"
      + "      <childName name=\"test\">child_name</childName>\n"
      + "      <subParent>\n"
      + "         <subChild>subChild_result</subChild>\n"
      + "      </subParent>\n"
      + "   </parent>\n"
      + "</root>\n";

  public static final String XML_WITH_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
      + "<root xmlns:test=\"www.test.com\">\n"
      + "   <test:xpath1>xpath1_result</test:xpath1>\n"
      + "   <test:xpath2>xpath2_result</test:xpath2>\n"
      + "   <test:xpath3>xpath3_result</test:xpath3>\n"
      + "   <test:parent>\n"
      + "      <test:child1>child1</test:child1>\n"
      + "      <test:child2>child2</test:child2>\n"
      + "   </test:parent>\n"
      + "</root>\n";

  private static final String NOT_XML = "not xml";

  private static final String XPATH_WITH_ATTRIBUTE = "//parent/childName/@name";
  private static final String XPATH_1_NO_NAMESPACE = "//xpath1";
  private static final String XPATH_PARENT_NAMESPACE = "//parent";
  private static final String XPATH_1_WITH_NAMESPACE = "//test:xpath1";
  private static final String XPATH_2_NO_NAMESPACE = "//xpath2";
  private static final String XPATH_2_WITH_NAMESPACE = "//test:xpath2";
  private static final String XPATH_3_NO_NAMESPACE = "//xpath3";
  private static final String XPATH_3_WITH_NAMESPACE = "//test:xpath3";
  private static final String XPATH_NON_EXISTENT = "//doesNotExist";
  private static final String XPATH_INVALID = "invalid xpath";

  private static Map<String, String> resultKeyValuePairs;
  private static List<String> xpath;
  private static XpathBuilder xpathProvider;

  @BeforeAll
  public static void setUp() {
    resultKeyValuePairs = new LinkedHashMap<>();
    xpath = new ArrayList<>();
    xpathProvider = new XpathBuilder();
  }

  @AfterEach
  public void tearDown() {
    resultKeyValuePairs.clear();
    xpath.clear();
  }

  @Test
  public void testSingleXpathNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_NO_NAMESPACE, msg.getContent());
  }

  @Test
  public void testParentXpathNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_PARENT_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_NO_NAMESPACE, msg.getContent());
  }

  @Test
  public void testXpathWithAttribute() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_WITH_ATTRIBUTE);
    xpathProvider.setXpaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_NO_NAMESPACE, msg.getContent());
  }

  @Test
  public void testMultipleXpathsNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpath.add(XPATH_2_NO_NAMESPACE);
    xpath.add(XPATH_3_NO_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_NO_NAMESPACE, msg.getContent());
  }

  @Test
  public void testSingleXpathWithNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    xpath.add(XPATH_1_WITH_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    xpathProvider.setNamespaceContext(createContextEntries());
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_WITH_NAMESPACE, msg.getContent());
  }

  @Test
  public void testMultipleXpathsWithNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    xpath.add(XPATH_1_WITH_NAMESPACE);
    xpath.add(XPATH_2_WITH_NAMESPACE);
    xpath.add(XPATH_3_WITH_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    xpathProvider.setNamespaceContext(createContextEntries());
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_WITH_NAMESPACE, msg.getContent());
  }

  @Test
  public void testMetadataXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    msg.addMetadata("xPath", XPATH_1_NO_NAMESPACE);
    xpath.add("%message{xPath}");
    xpathProvider.setXpaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_NO_NAMESPACE, msg.getContent());
  }

  @Test
  public void testMixtureOfXpaths() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    msg.addMetadata("xPath", XPATH_1_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpath.add(XPATH_2_NO_NAMESPACE);
    xpath.add(XPATH_WITH_ATTRIBUTE);
    xpath.add("%message{xPath}");
    xpathProvider.setXpaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(XML_NO_NAMESPACE, msg.getContent());
  }

  @Test
  public void testExtractingNonExistentXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_NON_EXISTENT);
    xpathProvider.setXpaths(xpath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    }, "Xpath does not exist.");
  }

  @Test
  public void testExtractingInvalidXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_INVALID);
    xpathProvider.setXpaths(xpath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    }, "Invalid Xpath.");
  }

  @Test
  public void testExtractingNonXml() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(NOT_XML);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    }, "Non xml message.");
  }

  @Test
  public void testExtractingMisMatchedNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    xpath.add(XPATH_1_WITH_NAMESPACE);
    xpathProvider.setXpaths(xpath);
    KeyValuePairSet contextEntries = new KeyValuePairSet();
    contextEntries.add(new KeyValuePair("wrong", "wrong"));
    xpathProvider.setNamespaceContext(contextEntries);
    assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    }, "Xpath not found due to unexpected namespace name.");
  }

  @Test
  public void testInsertingNonExistentXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    resultKeyValuePairs.put(XPATH_NON_EXISTENT, "Some value");
    assertThrows(ServiceException.class, () -> {
      xpathProvider.insert(msg, resultKeyValuePairs);
    }, "Xpath does not exist.");
  }

  @Test
  public void testInsertingInvalidXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    resultKeyValuePairs.put(XPATH_INVALID, "Some value");
    assertThrows(ServiceException.class, () -> {
      xpathProvider.insert(msg, resultKeyValuePairs);
    }, "Invalid Xpath.");
  }

  @Test
  public void testInsertingNonXml() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(NOT_XML);
    resultKeyValuePairs.put(XPATH_1_NO_NAMESPACE, "Some value");
    assertThrows(ServiceException.class, () -> {
      xpathProvider.insert(msg, resultKeyValuePairs);
    }, "Not an XML file.");
  }

  private static KeyValuePairSet createContextEntries() {
    KeyValuePairSet contextEntries = new KeyValuePairSet();
    contextEntries.add(new KeyValuePair("test", "www.test.com"));
    return contextEntries;
  }

}