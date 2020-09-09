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

import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
    aggregate(msg, msgs);
  }

  @Override
  public void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> messages)
      throws CoreException {
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(original.getOutputStream())) {
      for (AdaptrisMessage m : messages) {
        if (BooleanUtils.and(new boolean[] {filter(m), m.headersContainsKey(filenameMetadata())})) {
          zipOutputStream.putNextEntry(new ZipEntry(m.getMetadataValue(filenameMetadata())));
          zipOutputStream.write(m.getPayload());
          zipOutputStream.closeEntry();
        }
      }
    } catch (Exception e) {
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

  protected String filenameMetadata() {
    return StringUtils.defaultIfBlank(getFilenameMetadata(), DEFAULT_FILENAME_METADATA);
  }
}
