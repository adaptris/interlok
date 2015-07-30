package com.adaptris.util.text.xml;

import java.io.InputStream;
import java.io.Reader;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Stuart Ellidge
 * 
 * Convenience class which validates an XML document against a specified schema
 */
public class Validator {
  private DOMParser parser = null;

  /**
   * Constructor that creates a new Validator object
   * 
   * @param schema a valid url to the schema to process
   * @throws Exception if a parse error is encountered
   */
  public Validator(String schema) throws Exception {
    parser = new DOMParser();

    Class poolClass = Class
        .forName("org.apache.xerces.util.XMLGrammarPoolImpl");
    Object grammarPool = poolClass.newInstance();
    parser.setProperty(
        "http://apache.org/xml/properties/internal/grammar-pool", grammarPool);

    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
    parser.setProperty("http://apache.org/xml/properties/schema/"
        + "external-noNamespaceSchemaLocation", schema);
    parser.setErrorHandler(new ErrorChecker());
  }

  /**
   * Constructor that creates a new Validator object
   * 
   * @param schema a valid url to the schema to process
   * @param resolver an EntityResolver to use to retrieve included / imported
   *          documents
   * @throws Exception if a parse error is encountered
   */
  public Validator(String schema, EntityResolver resolver) throws Exception {
    parser = new DOMParser();

    Class poolClass = Class
        .forName("org.apache.xerces.util.XMLGrammarPoolImpl");
    Object grammarPool = poolClass.newInstance();
    parser.setProperty(
        "http://apache.org/xml/properties/internal/grammar-pool", grammarPool);

    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
    parser.setProperty("http://apache.org/xml/properties/schema"
        + "/external-noNamespaceSchemaLocation", schema);
    parser.setErrorHandler(new ErrorChecker());
    parser.setEntityResolver(resolver);
  }

  /**
   * method which parses an xml document from an input stream, validates it and
   * returns the subsequent Document object.
   * 
   * @param xml the xml document as input stream
   * @return the resultant Document (if parsing successful)
   * @throws Exception if the document fails to be validated
   */
  public Document parse(InputStream xml) throws Exception {
    parse(new InputSource(xml));
    return parser.getDocument();
  }

  /**
   * method which parses an xml document from a Reader, validates it and returns
   * the subsequent Document object.
   * 
   * @param xml the xml document as reader
   * @return the resultant Document (if parsing successful)
   * @throws Exception if the document fails to be validated
   */
  public Document parse(Reader xml) throws Exception {
    parse(new InputSource(xml));
    return parser.getDocument();
  }

  /**
   * method which parses an xml document from an input source, validates it and
   * returns the subsequent Document object.
   * 
   * @param xml the xml document as input source
   * @return the resultant Document (if parsing successful)
   * @throws Exception if the document fails to be validated
   */
  public Document parse(InputSource xml) throws Exception {
    parser.parse(xml);
    return parser.getDocument();
  }

  private class ErrorChecker implements ErrorHandler {

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
}
