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

package com.adaptris.util.text.xml;

import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sf.joost.trax.TransformerFactoryImpl;

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
public class StxTransformerFactory extends XmlTransformerFactoryImpl {

  @Override
  public Transformer createTransformerFromRawXsl(String xsl, EntityResolver entityResolver) throws Exception {
    StreamSource xslStream = new StreamSource(new StringReader(xsl));
    return configure( new TransformerFactoryImpl()).newTransformer(xslStream);
  }

  @Override
  public XmlTransformer configure(XmlTransformer xmlTransformer) throws Exception {
    return xmlTransformer;
  }

}
