package com.adaptris.util.text.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * The XsltTransformerFactory is responsible for creating the {@link Transformer}.
 * </p>
 * <p>
 * The {@link Transformer} is used to actually perform a document transformation.
 * </p>
 * 
 * @config xslt-transformer-factory
 * 
 * @author amcgrath
 */

@XStreamAlias("xslt-transformer-factory")
public class XsltTransformerFactory implements XmlTransformerFactory {

  public Transformer createTransformer(String url) throws Exception {
    return this.createTransformer(url, null);
  }

  public Transformer createTransformer(String url, EntityResolver entityResolver) throws Exception {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    dfactory.setCoalescing(true);
    dfactory.setNamespaceAware(true);

    DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    if (entityResolver != null) {
      docBuilder.setEntityResolver(entityResolver);
    }
    Document xmlDoc = docBuilder.parse(new InputSource(url));

    return TransformerFactory.newInstance().newTransformer(new DOMSource(xmlDoc, url));
  }

}
