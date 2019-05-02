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

package com.adaptris.core.common;

import static com.adaptris.core.common.MetadataDataOutputParameter.DEFAULT_METADATA_KEY;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.InterlokMessage.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataOutputParameter} is used when you want to write some data to {@link com.adaptris.core.AdaptrisMessage}
 * metadata.
 * 
 * @config metadata-stream-output-parameter
 * 
 */
@XStreamAlias("metadata-stream-output-parameter")
@DisplayOrder(order = {"metadataKey", "contentEncoding"})
public class MetadataStreamOutputParameter extends MetadataStreamParameter
    implements DataOutputParameter<InputStreamWithEncoding>,
    MessageWrapper<OutputStream> {
    
  public MetadataStreamOutputParameter() {
    super();
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  public MetadataStreamOutputParameter(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public void insert(InputStreamWithEncoding data, InterlokMessage message) throws InterlokException {
    try {
      StringBuilder builder = new StringBuilder();
      try (
          Reader in = new InputStreamReader(data.inputStream,
              charset(defaultIfEmpty(getContentEncoding(), data.encoding)));
          StringBuilderWriter out = new StringBuilderWriter(builder)) {
        IOUtils.copy(in, out);
      }
      message.addMessageHeader(getMetadataKey(), builder.toString());
    } catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public OutputStream wrap(InterlokMessage m) throws Exception {
    return new MetadataOutputStream(m, new StringWriter());
  }

  private class MetadataOutputStream extends FilterOutputStream {

    private InterlokMessage msg;
    private StringWriter writer;

    public MetadataOutputStream(InterlokMessage msg, StringWriter writer) {
      super(new WriterOutputStream(writer, charset(getContentEncoding())));
      this.msg = msg;
      this.writer = writer;
    }

    @Override
    public void close() throws IOException {
      super.close();
      msg.addMessageHeader(getMetadataKey(), writer.toString());
    }
  }
}
