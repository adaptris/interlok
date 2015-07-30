package com.adaptris.util.text.xml;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.joost.trax.TransformerFactoryImpl;

import org.xml.sax.EntityResolver;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * The StxTransformerFactory is responsible for creating the {@link Transformer}.
 * </p>
 * <p>
 * The {@link Transformer} is used to actually perform a document transformation. This particular implementation will create a
 * Transformer specifically for STX type transforms.
 * </p>
 * 
 * @config stx-transform-factory
 * 
 * @author amcgrath
 */

@XStreamAlias("stx-transformer-factory")
public class StxTransformerFactory implements XmlTransformerFactory {

  @Override
  public Transformer createTransformer(String xsl) throws Exception {
    TransformerFactory factory = new TransformerFactoryImpl();
    StreamSource xslStream = new StreamSource(xsl);
    
    return factory.newTransformer(xslStream);
  }

  @Override
  public Transformer createTransformer(String url, EntityResolver entityResolver) throws Exception {
    return this.createTransformer(url);
  }

}
