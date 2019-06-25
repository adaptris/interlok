/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.common;

import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.MessageWrapper;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.SimpleByteTranslator;

public abstract class ByteArrayFromMetadataWrapper implements MessageWrapper<byte[]> {

  @NotBlank
  private String key;
  @AdvancedConfig
  @Valid
  @InputFieldDefault(value = "SimpleByteTranslator")
  private ByteTranslator translator;

  public ByteArrayFromMetadataWrapper() {

  }

  public String getKey() {
    return key;
  }

  /**
   * Specify the key to retrieve from.
   * 
   * @param key
   */
  public void setKey(String key) {
    this.key = Args.notBlank(key, "key");
  }


  public ByteTranslator getTranslator() {
    return translator;
  }

  /**
   * Set the translator that will give us bytes.
   * 
   * @param t
   */
  public void setTranslator(ByteTranslator t) {
    this.translator = t;
  }

  private ByteTranslator translator() {
    return ObjectUtils.defaultIfNull(getTranslator(), new SimpleByteTranslator());
  }

  public <T extends ByteArrayFromMetadataWrapper> T withKey(String s) {
    setKey(s);
    return (T) this;
  }

  public <T extends ByteArrayFromMetadataWrapper> T withTranslator(ByteTranslator s) {
    setTranslator(s);
    return (T) this;
  }

  protected byte[] toByteArray(String s) throws Exception {
    if (StringUtils.isBlank(s)) {
      return new byte[0];
    }
    return translator().translate(s);
  }

}
