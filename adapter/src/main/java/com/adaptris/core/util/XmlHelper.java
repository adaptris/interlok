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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.util.XmlUtils;

/**
 * Helper class for handling XML within an {@linkplain com.adaptris.core.AdaptrisMessage}
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

  
  private static final String[] INVALID_ELEMENT_CHARS =
  {
      "\\\\", "\\?", "\\*", "\\:", " ", "\\|", "&", "\\\"", "\\'", "<", ">", "\\)", "\\(", "\\/", "#"
  };
  private static final String ELEM_REPL_VALUE = "_";

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

    try (InputStream input = msg.getInputStream()) {
      DocumentBuilderFactory dbf = DocumentBuilderFactoryBuilder.newInstance(builder).withNamespaceAware(ctx).build();
      result = new XmlUtils(ctx, dbf);
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
    return createDocument(msg.getInputStream(), builder);
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
    DocumentBuilder builder = DocumentBuilderFactoryBuilder.newInstance(cfg).build().newDocumentBuilder();
    builder.setErrorHandler(new DefaultErrorHandler());
    return builder;
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
    return createDocument(s, DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(namespaceAware));
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

    try (StringReader in = new StringReader(s)) {
      result = newDocumentBuilder(builder).parse(new InputSource(in));
    }
    return result;
  }

  /**
   * Create a document from an {@code InputStream}.
   * 
   * @param in the inputstream
   * @param builder configuration for the underlying {@link DocumentBuilderFactory} instance..
   * @return the Document element
   */
  public static Document createDocument(InputStream in, DocumentBuilderFactoryBuilder builder)
      throws ParserConfigurationException, IOException, SAXException {
    Document result = null;
    try (InputStream docIn = in) {
      result = newDocumentBuilder(builder).parse(new InputSource(docIn));
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
      if (!XMLChar.isNameStart((name.charAt(0)))) {
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

  /**
   * Write an XML document to the message with specified encoding.
   * 
   * @param doc the document
   * @param msg the message
   * @param encoding, will default to "UTF-8" if not specified, and the msg does not have a declared content encoding.
   * @throws Exception
   */
  public static void writeXmlDocument(Document doc, AdaptrisMessage msg, String encoding) throws Exception {
    String encodingToUse = getXmlEncoding(msg, encoding);
    try (OutputStream out = msg.getOutputStream()) {
      new XmlUtils().writeDocument(doc, out, encodingToUse);
    }
    msg.setContentEncoding(encodingToUse);
  }

  /**
   * Figure out what encoding to use when writing a document.
   * 
   * @param msg the message
   * @param enc the configured encoding, if any.
   * @return either the value of {@code encoding}, {@code AdaptrisMessage#getContentEncoding()} or 'UTF-8' in that order or
   *         preference.
   */
  public static String getXmlEncoding(AdaptrisMessage msg, String enc) {
    String encoding = "UTF-8";
    if (!isBlank(enc)) {
      encoding = enc;
    } else if (!isBlank(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
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
