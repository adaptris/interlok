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

import static com.adaptris.core.common.MetadataDataInputParameter.DEFAULT_METADATA_KEY;
import java.io.InputStream;
import java.io.StringReader;
import org.apache.commons.io.input.ReaderInputStream;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.InterlokMessage.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataInputParameter} is used when you want to read some data from metadata.
 * 
 * @config metadata-stream-input-parameter
 * 
 */
@XStreamAlias("metadata-stream-input-parameter")
@DisplayOrder(order = {"metadataKey", "contentEncoding"})
public class MetadataStreamInputParameter extends MetadataStreamParameter
    implements DataInputParameter<InputStream>, MessageWrapper<InputStream> {

  public MetadataStreamInputParameter() {
    this.setMetadataKey(DEFAULT_METADATA_KEY);
  }
  
  public MetadataStreamInputParameter(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public InputStream extract(InterlokMessage m) throws InterlokException {
    Args.notBlank(getMetadataKey(), "metadataKey");
    String data= m.getMessageHeaders().get(getMetadataKey());
    return new ReaderInputStream(new StringReader(data), charset(getContentEncoding()));
  }

  @Override
  public InputStream wrap(InterlokMessage m) throws Exception {
    return extract(m);
  }

}
