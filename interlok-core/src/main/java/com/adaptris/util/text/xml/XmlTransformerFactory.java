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

import javax.xml.transform.Transformer;

import org.xml.sax.EntityResolver;

public interface XmlTransformerFactory {

  /**
   * @deprecated You should use {@link XmlTransformerFactory#createTransformerFromUrl(String)} instead
   */
  @Deprecated
  default Transformer createTransformer(String transformUrl) throws Exception {
    return createTransformerFromUrl(transformUrl);
  }

  /**
   * @deprecated You should use {@link XmlTransformerFactory#createTransformerFromUrl(String, EntityResolver)} instead
   */
  @Deprecated
  default Transformer createTransformer(String transformUrl, EntityResolver entityResolver) throws Exception {
    return createTransformerFromUrl(transformUrl, entityResolver);
  }

  Transformer createTransformerFromUrl(String transformUrl) throws Exception;

  Transformer createTransformerFromUrl(String transformUrl, EntityResolver entityResolver) throws Exception;

  Transformer createTransformerFromRawXsl(String xsl) throws Exception;

  Transformer createTransformerFromRawXsl(String xsl, EntityResolver entityResolver) throws Exception;

  XmlTransformer configure(XmlTransformer xmlTransformer) throws Exception;
}
