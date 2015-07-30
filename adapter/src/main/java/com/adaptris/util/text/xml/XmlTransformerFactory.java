package com.adaptris.util.text.xml;

import javax.xml.transform.Transformer;

import org.xml.sax.EntityResolver;

public interface XmlTransformerFactory {

  Transformer createTransformer(String transformUrl) throws Exception;
  
  Transformer createTransformer(String transformUrl, EntityResolver entityResolver) throws Exception;
  
}
