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

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.CharsetByteTranslator;
import com.adaptris.util.text.HexStringByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class will encode a metadata value into its equivalent hex encoded metadata value(s) using the specified character
 * encoding
 * 
 * @config string-to-hex-metadata-service
 * 
 * @see HexToStringService
 * 
 */
@XStreamAlias("string-to-hex-metadata-service")
@ComponentProfile(summary = "Turn a metadata value into a hex string using the specified character encoding",
    tag = "service,metadata")
@DisplayOrder(order = {"metadataKeyRegexp", "charset", "metadataLogger"})
public class StringToHexService extends HexToStringService {

  public StringToHexService() {
    super();
  }

  public StringToHexService(String regex) {
    super(regex);
    setCharset(UTF_8);
  }

  @Override
  public String reformat(String s, String msgCharset) throws Exception {
    ByteTranslator bytesToHex = new HexStringByteTranslator();
    ByteTranslator stringToBytes = new CharsetByteTranslator(getCharacterEncoding());
    return bytesToHex.translate(stringToBytes.translate(s));
  }

}
