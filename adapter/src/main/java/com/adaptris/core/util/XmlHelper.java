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

package com.adaptris.core.util;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.XmlUtils;

/**
 * Helper class for handling XML within an {@linkplain AdaptrisMessage}
 *
 * @author lchan
 * @author $Author: $
 */
public class XmlHelper {
  // XML 1.0
  // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
  // private static final String XML10 = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff"
  // + "]";

  // XML 1.1
  // [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
  // private static final String XML11 = "[^" + "\u0001-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]+";

  private static final String ILLEGAL_XML = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff"
      + "]";

  private static final String VALID_ELEMENT_INITIAL_CHAR = "^[:A-Z_a-z\\u00C0\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02ff\\u0370-\\u037d"
      + "\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
      + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\x10000-\\xEFFFF].*";
  
  private static final String[] INVALID_ELEMENT_CHARS =
  {
      "\\\\", "\\?", "\\*", "\\:", " ", "\\|", "&", "\\\"", "\\'", "<", ">", "\\)", "\\(", "\\/", "#"
  };
  private static final String ELEM_REPL_VALUE = "_";

  private transient static Logger log = LoggerFactory.getLogger(XmlHelper.class);

  /**
   * Create an XMLUtils class from an AdaptrisMessage.
   * 
   * @param msg the Adaptris message
   * @return an XxmlUtils instance
   * @throws CoreException if the msg does not contain valid XML.
   * @deprecated Consider using {@link #createXmlUtils(AdaptrisMessage, NamespaceContext)} instead.
   */
  public static XmlUtils createXmlUtils(AdaptrisMessage msg) throws CoreException {
    return createXmlUtils(msg, null);
  }

  /**
   * Create an XMLUtils class from an AdaptrisMessage.
   * 
   * @deprecated since 3.1.0 use {{@link #createDocument(AdaptrisMessage, DocumentBuilderFactoryBuilder)} instead.
   * @param msg the Adaptris message
   * @param ctx the NamespaceContext.
   * @return an XxmlUtils instance
   * @throws CoreException if the msg does not contain valid XML.
   */
  @Deprecated
  public static XmlUtils createXmlUtils(AdaptrisMessage msg, NamespaceContext ctx) throws CoreException {
    return createXmlUtils(msg, ctx, DocumentBuilderFactoryBuilder.newInstance());
  }

  /**
   * Create an XMLUtils class from an AdaptrisMessage.
   * 
   * @param msg the Adaptris message
   * @param ctx the NamespaceContext.
   * @param builder configuration for the underlying {@link DocumentBuilderFactory} instance..
   * @return an XmlUtils instance
   * @throws CoreException if the msg does not contain valid XML.
   */
  public static XmlUtils createXmlUtils(AdaptrisMessage msg, NamespaceContext ctx, DocumentBuilderFactoryBuilder builder)
      throws CoreException {
    XmlUtils result = null;
    DivertConsoleOutput dc = new DivertConsoleOutput();
    InputStream input = null;
    try {
      DocumentBuilderFactory dbf = builder.configure(DocumentBuilderFactory.newInstance());
      if (ctx != null) {
        dbf.setNamespaceAware(true);
      }
      result = new XmlUtils(ctx, dbf);
      input = msg.getInputStream();
      InputSource in = new InputSource(input);
      // Well what we're going to do here is annoyingly bad, but I want to eat
      // those stupid System.err messages that contain shit like
      // "[Fatal Error] :1:1: Content is not allowed in prolog"
      dc.divert();
      result.setSource(in);
    } catch (Exception e) {
      result = null;
    } finally {
      dc.resume();
      IOUtils.closeQuietly(input);
    }
    if (dc.consoleOutput().contains("[Fatal Error]") || result == null) {
      throw new CoreException(
          "Document " + msg.getUniqueId() + " does not appear to be an XML Document, error was [" + dc.consoleOutput() + "]");
    }
    return result;
  }
  /**
   * Create a document from an AdaptrisMessage.
   * 
   * @see #createDocument(AdaptrisMessage, boolean)
   * @deprecated Consider using {@link #createDocument(AdaptrisMessage, NamespaceContext)} instead.
   */
  @Deprecated
  public static Document createDocument(AdaptrisMessage msg) throws ParserConfigurationException, IOException, SAXException {
    return createDocument(msg, false);
  }

  /**
   * Create a document from an AdaptrisMessage.
   * @deprecated since 3.1.0 use {{@link #createDocument(AdaptrisMessage, DocumentBuilderFactoryBuilder)} instead.
   * @param msg the AdaptrisMessage
   * @param namespaceAware whether or not the document builder factory used will be namespace aware.
   * @return the Document element
   */
  @Deprecated
  public static Document createDocument(AdaptrisMessage msg, boolean namespaceAware)
      throws ParserConfigurationException, IOException, SAXException {
    return createDocument(msg, DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(namespaceAware));
  }

  /**
   * Create a document from an AdaptrisMessage.
   * @param msg the AdaptrisMessage
   * @param builder configuration for the underlying {@link DocumentBuilderFactory} instance..
   * @return the Document element
   */
  public static Document createDocument(AdaptrisMessage msg, DocumentBuilderFactoryBuilder builder)
      throws ParserConfigurationException, IOException, SAXException {
    Document result = null;
    InputStream in = null;
    try {
      in = msg.getInputStream();
      result = newDocumentBuilder(builder).parse(new InputSource(in));
    } finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }


  /**
   * Convenience method to create a document from an AdaptrisMessage.
   * @deprecated since 3.1.0 use {{@link #createDocument(AdaptrisMessage, DocumentBuilderFactoryBuilder)} instead.
   * @param msg the AdaptrisMessage
   * @param namespaceContext the name space context, if null, then we will not be namespace aware.
   * @return the Document element
   */
  @Deprecated
  public static Document createDocument(AdaptrisMessage msg, NamespaceContext namespaceContext)
      throws ParserConfigurationException, IOException, SAXException {
    return createDocument(msg, namespaceContext != null ? true : false);
  }

  private static DocumentBuilder newDocumentBuilder(DocumentBuilderFactoryBuilder cfg) throws ParserConfigurationException {
    DocumentBuilderFactoryBuilder b = defaultIfNull(cfg);
    DocumentBuilderFactory domFactory = b.configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    builder.setErrorHandler(new DefaultErrorHandler());
    return builder;
  }

  private static DocumentBuilderFactoryBuilder defaultIfNull(DocumentBuilderFactoryBuilder b) {
    return b != null ? b : DocumentBuilderFactoryBuilder.newInstance();
  }

  /**
   * Create a document from a String.
   *
   * @deprecated since 3.1.0 use {{@link #createDocument(String, DocumentBuilderFactoryBuilder)} instead.
   * @param s the string containing XML
   * @return the Document element
   */
  @Deprecated
  public static Document createDocument(String s) throws ParserConfigurationException, IOException, SAXException {
    return createDocument(s, false);
  }

  /**
   * Convenience method to create a document from an AdaptrisMessage.
   *
   * @deprecated since 3.1.0 use {{@link #createDocument(String, DocumentBuilderFactoryBuilder)} instead.
   * @param s the string containing XML
   * @param namespaceContext the namespace context, if null, then we will not be namespace aware.
   * @return the Document element
   */
  @Deprecated
  public static Document createDocument(String s, NamespaceContext namespaceContext)
      throws ParserConfigurationException, IOException, SAXException {
    return createDocument(s, namespaceContext != null ? true : false);
  }

  /**
   * Create a document from a String.
   *
   * @deprecated since 3.1.0 use {{@link #createDocument(String, DocumentBuilderFactoryBuilder)} instead.
   * @param s the string containing XML
   * @param namespaceAware whether or not the document builder factory used will be namespace aware.
   * @return the Document element
   */
  @Deprecated
  public static Document createDocument(String s, boolean namespaceAware)
      throws ParserConfigurationException, IOException, SAXException {
    return createDocument(s, new DocumentBuilderFactoryBuilder().withNamespaceAware(namespaceAware));
  }

  /**
   * Create a document from an AdaptrisMessage.
   * @param s the string containing XML
   * @param builder configuration for the underlying {@link DocumentBuilderFactory} instance..
   * @return the Document element
   */
  public static Document createDocument(String s, DocumentBuilderFactoryBuilder builder)
      throws ParserConfigurationException, IOException, SAXException {
    Document result = null;
    StringReader in = new StringReader(s);
    try {
      result = newDocumentBuilder(builder).parse(new InputSource(in));
    } finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }
  /**
   * Make a safe element name by stripping out illegal XML characters and illegal element characters.
   * 
   * @param input the input string.
   * @param defaultIfBlank the default element name if the input resolves to the blank string (whitespace is trimmed).
   * @return a safe element name that conforms to the specification.
   */
  public static String safeElementName(String input, String defaultIfBlank) {
    String name = defaultIfBlank(input, "").trim();
    name = name.replaceAll(ILLEGAL_XML, "");
    if (isBlank(name)) {
      name = defaultIfBlank;
    } else {
      if (!name.matches(VALID_ELEMENT_INITIAL_CHAR)) {
        name = ELEM_REPL_VALUE + name;
      }
      for (String invalid : INVALID_ELEMENT_CHARS) {
        name = name.replaceAll(invalid, ELEM_REPL_VALUE);
      }
    }
    return name;
  }

  /**
   * Strip illegal XML characters from data.
   * <p>
   * The following regular expression is used to strip out all invalid XML 1.0 characters :
   * <code>"[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff]"</code>.
   * </p>
   * 
   * @param input the input string
   * @return a string where all the invalid characters have been removed.
   */
  public static String stripIllegalXmlCharacters(String input) {
    return input.replaceAll(ILLEGAL_XML, "");
  }

  private static class DefaultErrorHandler implements ErrorHandler {

    @Override
    public void error(SAXParseException e) throws SAXException {
      throw e;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      throw e;
    }
  }

  private static final class DivertConsoleOutput {

    private PrintStream stderr, stdout, divert;
    private ByteArrayOutputStream out;

    DivertConsoleOutput() {
      stderr = System.err;
      stdout = System.out;
      out = new ByteArrayOutputStream();
      divert = new PrintStream(out, true);
    }

    void divert() {
      try {
        System.setErr(divert);
        System.setOut(divert);
      }
      catch (SecurityException ignored) {
        ;
      }

    }

    void resume() {
      try {
        System.setErr(stderr);
        System.setOut(stdout);
      }
      catch (SecurityException ignored) {
        ;
      }
      divert.flush();
    }

    String consoleOutput() {
      return out.toString();
    }
  }
}
