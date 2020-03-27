/*
 * Copyright 2020 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adaptris.core.common;

import static com.adaptris.core.common.MetadataDataOutputParameter.DEFAULT_METADATA_KEY;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.adaptris.util.text.ByteTranslator;
import com.adaptris.util.text.SimpleByteTranslator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageWrapper} implementation wraps a metadata value as an {@link OutputStream} along with a {@link ByteTranslator}
 * 
 * <p>
 * This can be used in a functionally equivalent way to {@link MetadataOutputStreamWrapper} and can be regarded as its replacement.
 * </p>
 * 
 * @config metadata-stream-output
 * @since 3.10.1
 */
@XStreamAlias("metadata-stream-output")
@DisplayOrder(order = {"metadataKey", "translator"})
@ComponentProfile(summary = "MessageWrapper implementation wraps a metadata value as an Outputstream", since = "3.10.1")
public class MetadataStreamOutput implements MessageWrapper<OutputStream> {

  @NotBlank
  private String metadataKey;
  @AdvancedConfig
  @Valid
  @InputFieldDefault(value = "SimpleByteTranslator")
  private ByteTranslator translator;

  public MetadataStreamOutput() {
    super();
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }

  @Override
  public OutputStream wrap(InterlokMessage m) throws Exception {
    return new MetadataOutputStream(m, new ByteArrayOutputStream());
  }


  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String key) {
    this.metadataKey = Args.notBlank(key, "metadata key");
  }

  public <T extends MetadataStreamOutput> T withMetadataKey(String e) {
    setMetadataKey(e);
    return (T) this;
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


  public <T extends MetadataStreamOutput> T withTranslator(ByteTranslator s) {
    setTranslator(s);
    return (T) this;
  }


  private class MetadataOutputStream extends FilterOutputStream {

    private InterlokMessage msg;
    private ByteArrayOutputStream byteOut;

    public MetadataOutputStream(InterlokMessage msg, ByteArrayOutputStream out) {
      super(out);
      this.msg = msg;
      this.byteOut = out;
    }

    @Override
    public void close() throws IOException {
      super.close();
      msg.addMessageHeader(getMetadataKey(), translator().translate(byteOut.toByteArray()));
    }
  }
}
