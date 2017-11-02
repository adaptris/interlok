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

package com.adaptris.core.lms;

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Message factory that creates file backed messages from ZIP files. The first entry in the zip 
 * file will become the contents of the {@link com.adaptris.core.AdaptrisMessage}. Any other entries in the file 
 * will be ignored. This is useful for processing large zip files without having to separately 
 * extract them before sending them in to the adapter.
 */
@XStreamAlias("zip-file-backed-message-factory")
@DisplayOrder(order = {"compressionMode", "fallback", "defaultCharEncoding", "tempDirectory", "maxMemorySizeBytes", "defaultBufferSize"})
public class ZipFileBackedMessageFactory extends FileBackedMessageFactory {

  public enum CompressionMode {
    /**
     * Compress mode will expect uncompressed data as message payload and write it to a 
     * zip file. Reading from the message in this mode will yield compressed data. 
     */
    Compress, 
    
    /**
     * Uncompress mode will expect compressed data as the message payload and yield
     * uncompressed data when the message is being read.
     */
    Uncompress, 
    
    /**
     * Both mode expects uncompressed data as message payload and yield uncompressed
     * data when reading the message back. The data is only compressed in the temporary
     * file.
     */
    Both
  }
  
  @InputFieldDefault(value = "Uncompress")
  private CompressionMode compressionMode;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean failFast;
  
  public ZipFileBackedMessageFactory() {
    setCompressionMode(CompressionMode.Uncompress);
  }
  
  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage m = new ZipFileBackedMessageImpl(uniqueIdGenerator(), this);
    if (!isEmpty(getDefaultCharEncoding())) {
      m.setContentEncoding(getDefaultCharEncoding());
    }
    return m;
  }

  public CompressionMode getCompressionMode() {
    return compressionMode;
  }

  /**
   * The compression mode of the ZipFileBackedMessage. Choose a mode depending on what you want to do:
   * <ul>
   *   <li>Compress - For creating zip files</li>
   *   <li>Uncompress - For reading from zip files</li>
   *   <li>Both - For saving temporary file space</li>
   * </ul>
   */
  public void setCompressionMode(CompressionMode compressionMode) {
    this.compressionMode = compressionMode;
  }

  public Boolean getFailFast() {
    return failFast;
  }

  /**
   * Whether or not we fail if the input is not a zip file.
   * 
   * @param failFast defaults to true if not specified. If set to false, then non-zip input is handled transparently.
   */
  public void setFailFast(Boolean failFast) {
    this.failFast = failFast;
  }
  
  boolean failFast() {
    return getFailFast() != null ? getFailFast().booleanValue() : true;
  }
}
