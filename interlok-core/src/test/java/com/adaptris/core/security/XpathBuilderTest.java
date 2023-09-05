package com.adaptris.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class XpathBuilderTest {

  public static final String XML_NO_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
      + "<root>\r\n"
      + "   <xpath1>xpath1_result</xpath1>\r\n"
      + "   <xpath2>xpath2_result</xpath2>\r\n"
      + "   <xpath3>xpath3_result</xpath3>\r\n"
      + "   <parent>\r\n"
      + "      <child1>child1</child1>\r\n"
      + "      <child2>child2</child2>\r\n"
      + "   </parent>\r\n"
      + "</root>\r\n";

  public static final String XML_WITH_NAMESPACE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root xmlns:test=\"www.test.com\"><test:xpath1>xpath1_result</test:xpath1><test:xpath2>xpath2_result</test:xpath2>"
      + "<test:xpath3>xpath3_result</test:xpath3></root>";

  private static final String NOT_XML = "not xml";

  private static final String XPATH_RELATIVE_ROOT = "./root";
  private static final String XPATH_1_NO_NAMESPACE = "//xpath1";
  private static final String XPATH_1_WITH_NAMESPACE = "//test:xpath1";
  private static final String XPATH_2_NO_NAMESPACE = "//xpath2";
  private static final String XPATH_2_WITH_NAMESPACE = "//test:xpath2";
  private static final String XPATH_3_NO_NAMESPACE = "//xpath3";
  private static final String XPATH_3_WITH_NAMESPACE = "//test:xpath3";
  private static final String XPATH_NESTED = "//parent";
  private static final String XPATH_NON_EXISTENT = "//doesNotExist";
  private static final String XPATH_INVALID = "invalid xpath";
  
  private static final String NON_XML_EXCEPTION_MESSAGE = "Unable to create XML document";
  private static final String INVALID_XPATH_EXCEPTION_MESSAGE = "Unable to evaluate if Xpath exists, please ensure the Xpath is valid";
  private static final String XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE = "XPath [%s] does not match any nodes";
  
  private static Map<String, String> resultKeyValuePairs;
  private static List<String> xpath;
  private static XpathBuilder xpathProvider;
  
  @BeforeAll
  public static void setUp() {
    resultKeyValuePairs  = new LinkedHashMap<>();
    xpath = new ArrayList<String>();
    xpathProvider = new XpathBuilder();
  }
  
  @AfterEach
  public void tearDown() {
    resultKeyValuePairs.clear();
    xpath.clear();
  }

  @Test
  public void testExtractingRelativeXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_RELATIVE_ROOT);
    xpathProvider.setPaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(1, resultKeyValuePairs.size(), "LinkedHashMap should have 1 entry");
  }
  
  @Test
  public void testExtractingSingleXpathNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpathProvider.setPaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      System.out.println(resultKeyValuePairs + "test");
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(1, resultKeyValuePairs.size(), "LinkedHashMap should have 1 entry");
  }

  @Test
  public void testExtractingMultipleXpathsNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpath.add(XPATH_2_NO_NAMESPACE);
    xpath.add(XPATH_3_NO_NAMESPACE);
    xpathProvider.setPaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(3, resultKeyValuePairs.size(), "LinkedHashMap should have 3 entries");
  }

  @Test
  public void testExtractingSingleXpathWithNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    xpath.add(XPATH_1_WITH_NAMESPACE);
    xpathProvider.setPaths(xpath);
    xpathProvider.setNamespaceContext(createContextEntries());
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(1, resultKeyValuePairs.size(), "LinkedHashMap should have 1 entry");
  }

  @Test
  public void testExtractingMultipleXpathsWithNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_WITH_NAMESPACE);
    xpath.add(XPATH_1_WITH_NAMESPACE);
    xpath.add(XPATH_2_WITH_NAMESPACE);
    xpath.add(XPATH_3_WITH_NAMESPACE);
    xpathProvider.setPaths(xpath);
    xpathProvider.setNamespaceContext(createContextEntries());
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(3, resultKeyValuePairs.size(), "LinkedHashMap should have 3 entries");
  }
  
  @Test
  public void testExtractingNestedXpaths() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_NESTED);
    xpathProvider.setPaths(xpath);
    xpathProvider.setNamespaceContext(createContextEntries());
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    assertEquals(1, resultKeyValuePairs.size(), "LinkedHashMap should have 1 entry");
  }

  @Test
  public void testExtractingNonExistentXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_NON_EXISTENT);
    xpathProvider.setPaths(xpath);
    Throwable exception = Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    });
    assertEquals(String.format(XPATH_DOES_NOT_EXIST_EXCEPTION_MESSAGE, XPATH_NON_EXISTENT), exception.getMessage());
  }

  @Test
  public void testExtractingInvalidXpath() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_INVALID);
    xpathProvider.setPaths(xpath);
    Throwable exception = Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    });
    assertEquals(INVALID_XPATH_EXCEPTION_MESSAGE, exception.getMessage());
  }

  @Test
  public void testNonXml() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(NOT_XML);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpathProvider.setPaths(xpath);
    Throwable exception =  Assertions.assertThrows(ServiceException.class, () -> {
      resultKeyValuePairs = xpathProvider.extract(msg);
    });
    assertEquals(NON_XML_EXCEPTION_MESSAGE, exception.getMessage());
  }
  
  @Test
  public void testSingleXpathNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    AdaptrisMessage originalMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpathProvider.setPaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    System.out.println("original msg = " + originalMsg.getContent());
    System.out.println("new msg = " + msg.getContent());
    //assertEquals(msg.getContent(), originalMsg.getContent());
  }
  
  @Test
  public void testMultipleXpathsNoNameSpace() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    AdaptrisMessage originalMsg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_NO_NAMESPACE);
    xpath.add(XPATH_1_NO_NAMESPACE);
    xpath.add(XPATH_2_NO_NAMESPACE);
    xpath.add(XPATH_3_NO_NAMESPACE);
    xpathProvider.setPaths(xpath);
    try {
      resultKeyValuePairs = xpathProvider.extract(msg);
      xpathProvider.insert(msg, resultKeyValuePairs);
    } catch (ServiceException e) {
      e.printStackTrace();
      fail();
    }
    System.out.println("original msg = " + originalMsg.getContent());
    System.out.println("new msg = " + msg.getContent() + "end");
  }
  
  private static KeyValuePairSet createContextEntries() {
    KeyValuePairSet contextEntries = new KeyValuePairSet();
    contextEntries.add(new KeyValuePair("test", "www.test.com"));
    return contextEntries;
  }

}
