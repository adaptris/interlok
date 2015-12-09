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

package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.CharsetByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class will decode hex encoded metadata value(s) using the specified character encoding
 * 
 * @config hex-to-string-metadata-service
 * 
 * 
 * @see StringToHexService
 */
@XStreamAlias("hex-to-string-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Turn a hex encoded string into a java string using the specified character encoding",
    tag = "service,metadata")
public class HexToStringService extends ReformatMetadata {

  private String charset;
  protected static final String UTF_8 = "UTF-8";

  public HexToStringService() {
    super();
  }

  public HexToStringService(String regex) {
    super(regex);
    setCharset(UTF_8);
  }


  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }
  public String getCharset() {
    return charset;
  }

  /**
   * The character encoding to be applied when decoding the hex string. If no encoding is specified, UTF8 will be used.
   *
   * @param encoding
   */
  public void setCharset(String encoding) {
    charset = encoding;
  }

  protected String getCharacterEncoding() {
    return defaultIfEmpty(getCharset(), UTF_8);
  }

  @Override
  protected String reformat(String s, String msgCharset) throws Exception {
    ByteTranslator hexToBytes = new HexStringByteTranslator();
    ByteTranslator bytesToString = new CharsetByteTranslator(getCharacterEncoding());
    return bytesToString.translate(hexToBytes.translate(s));
  }

}
