package com.adaptris.util.text.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

public interface XmlTransformer {
  
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   *          stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl, Map properties) throws Exception;
  
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   *          stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl, Map properties) throws Exception;

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @param properties optional Map of parameters to be passed to the stylesheet engine and that will be accessible from the
   *          stylesheet.
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl, Map properties) throws Exception;

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, InputStream xmlIn, OutputStream xmlOut, String xsl) throws Exception;

  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Reader xmlIn, Writer xmlOut, String xsl) throws Exception;
  /**
   * Transform method which converts an input XML document into another format using the specified XSLT
   * 
   * @param transformer the {@link Transformer}
   * @param xmlIn the input document
   * @param xmlOut the output document
   * @param xsl the stylesheet
   * @throws Exception in the event of parsing / transform errors
   */
  void transform(Transformer transformer, Source xmlIn, Result xmlOut, String xsl) throws Exception;

  
}
