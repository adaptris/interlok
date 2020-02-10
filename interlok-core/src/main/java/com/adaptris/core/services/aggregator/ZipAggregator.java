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

package com.adaptris.core.services.aggregator;

import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageAggregator} implementation that creates single zip using each message as a file in the zip.
 *
 * <p>
 * Use {@link #setFilenameMetadata(String)} to change the key that contains the filename to be used in the zip,
 * default: filename.
 * </p>
 *
 * <p>Each message returned by the split needs to set a value of key returned by {@link #getFilenameMetadata()}, if the
 * a value is not set the message will be ignored. If the filenames are not unique an exception will be thrown.</p>
 *
 * @config zip-aggregator
 * @author mwarman
 *
 */
@XStreamAlias("zip-aggregator")
@DisplayOrder(order = {"filenameMetadata", "overwriteMetadata" })
public class ZipAggregator extends MessageAggregatorImpl {

  public static final String DEFAULT_FILENAME_METADATA = "filename";

  @InputFieldDefault(value = "filename")
  private String filenameMetadata;

  public ZipAggregator(){
  }

  public ZipAggregator(final String filenameMetadata){
    setFilenameMetadata(filenameMetadata);
  }

  @Override
  public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) throws CoreException {
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(msg.getOutputStream())) {
      for (AdaptrisMessage message : filter(msgs)){
        if(message.getMessageHeaders().containsKey(filenameMetadata())) {
          zipOutputStream.putNextEntry(new ZipEntry(message.getMetadataValue(filenameMetadata())));
          zipOutputStream.write(message.getPayload());
          zipOutputStream.closeEntry();
        }
      }
    } catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }


  public void setFilenameMetadata(String filenameMetadata) {
    this.filenameMetadata = filenameMetadata;
  }

  /**
   * Returns the metadata key  which contains the respective filename.
   *
   * @return the metadata key which contains the respective filenames, default: filename.
   */
  public String getFilenameMetadata() {
    return filenameMetadata;
  }

  String filenameMetadata(){
    return getFilenameMetadata() != null ? getFilenameMetadata() : DEFAULT_FILENAME_METADATA;
  }
}
