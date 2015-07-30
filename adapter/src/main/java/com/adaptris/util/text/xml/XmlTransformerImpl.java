package com.adaptris.util.text.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A generic transformer.  With every method you will supply the {@link Transformer}, normally built by a factory {@link XmlTransformerFactory}.
 * </p>
 * 
 */
public class XmlTransformerImpl implements XmlTransformer {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  public XmlTransformerImpl() {
  }
  
  @Override
  public void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl, Map properties) throws Exception {
    if (properties != null) {
      transformer.clearParameters();
      Iterator<String> iter = properties.keySet().iterator();
      while (iter.hasNext()) {
        Object o = iter.next();
        transformer.setParameter(o.toString(), properties.get(o));
      }
    }

    transformer.setErrorListener(new ErrorListener() {
      @Override
      public void warning(TransformerException exception) throws TransformerException {
        log.warn("Warning in transformation", exception);
      }

      @Override
      public void error(TransformerException exception) throws TransformerException {
        throw exception;
      }

      @Override
      public void fatalError(TransformerException exception) throws TransformerException {
        throw exception;
      }
    });
    transformer.transform(xmlIn, xmlOut);
  }
  
  @Override
  public void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl, Map properties) throws Exception {
    transform(transformer, new StreamSource(xmlIn), new StreamResult(xmlOut), xsl, properties);
  }

  @Override
  public void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl, Map properties) throws Exception {
    transform(transformer, new StreamSource(xmlIn), new StreamResult(xmlOut), xsl, properties);
  }

  @Override
  public void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl) throws Exception {
    transform(transformer, new StreamSource(xmlIn), new StreamResult(xmlOut), xsl, null);
  }

  @Override
  public void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl) throws Exception {
    transform(transformer, new StreamSource(xmlIn), new StreamResult(xmlOut), xsl, null);
  }

  @Override
  public void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl) throws Exception {
    transform(transformer, xmlIn, xmlOut, xsl, null);
  }

}
