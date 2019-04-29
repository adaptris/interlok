/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.services.jdbc.types;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ColumnWriterWithCharEncoding implements ColumnWriter, ColumnTranslator {
  private String characterEncoding;
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  protected Reader toReader(InputStream in) throws UnsupportedEncodingException {
    return getCharacterEncoding() != null ? new InputStreamReader(in, getCharacterEncoding()) : new InputStreamReader(in);
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  /**
   * Set the character encoding used to convert the column into a String.
   *
   * @param charEnc the character encoding, if null then the platform encoding is assumed.
   */
  public void setCharacterEncoding(String charEnc) {
    characterEncoding = charEnc;
  }

}
