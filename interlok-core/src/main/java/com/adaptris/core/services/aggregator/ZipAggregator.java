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

import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@NoArgsConstructor
public class ZipAggregator extends MessageAggregatorImpl {

  public static final String DEFAULT_FILENAME_METADATA = "filename";

  /**
   * The metadata key that contains the filename to use in the zip file when aggregating.
   * <p>
   * This defaults to {@code 'filename'} if not explicitly configured.
   * </p>
   */
  @InputFieldDefault(value = "filename")
  @Getter
  @Setter
  private String filenameMetadata;

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
          try (InputStream in = m.getInputStream()) {
            IOUtils.copy(in, zipOutputStream);
          }
          zipOutputStream.closeEntry();
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }

  }

  private String filenameMetadata() {
    return StringUtils.defaultIfBlank(getFilenameMetadata(), DEFAULT_FILENAME_METADATA);
  }
}
