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

import static com.adaptris.core.util.MetadataHelper.convertToProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} that writes metadata to the filesystem.
 * </p>
 * <p>
 * Used in conjunction with {@link ReadMetadataFromFilesystem} to allow preservation of metadata across integration points that make
 * use of the filesystem.
 * </p>
 * 
 * @config write-metadata-to-filesystem
 * 
 * 
 */
@XStreamAlias("write-metadata-to-filesystem")
@AdapterComponent
@ComponentProfile(summary = "Write the current set of metadata to the filesystem", tag = "service,metadata")
@DisplayOrder(order =
{
    "destination", "outputStyle", "overwriteIfExists", "filenameCreator", "metadataFilter"
})
public class WriteMetadataToFilesystem extends ServiceImp {

  @Valid
  private FileNameCreator filenameCreator;
  private OutputStyle outputStyle;
  @NotNull
  @Valid
  private MessageDrivenDestination destination;
  @InputFieldDefault(value = "false")
  private Boolean overwriteIfExists;
  @AdvancedConfig
  @Valid
  private MetadataFilter metadataFilter;

  public enum OutputStyle {
    Text() {
      @Override
      void write(Collection<MetadataElement> p, OutputStream out) throws IOException {
        convertToProperties(p).store(out, "");
      }
    },
    XML() {
      @Override
      void write(Collection<MetadataElement> p, OutputStream out) throws IOException {
        convertToProperties(p).storeToXML(out, "");
      }
    };
    abstract void write(Collection<MetadataElement> p, OutputStream out) throws IOException;
  }

  public WriteMetadataToFilesystem() {
    super();
  }

  public WriteMetadataToFilesystem(MessageDrivenDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      String baseUrl = getDestination().getDestination(msg);
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      validateDir(url);
      File fileToWrite = new File(FsHelper.createFileReference(url), filenameCreator().createName(msg));
      if (overwriteIfExists()) {
        FileUtils.deleteQuietly(fileToWrite);
      }
      if (fileToWrite.exists()) {
        throw new IOException(fileToWrite.getCanonicalPath() + " already exists");
      }
      try (OutputStream out = new FileOutputStream(fileToWrite)) {
        getStyle(getOutputStyle()).write(metadataFilter().filter(msg.getMetadata()), out);
      }
      log.debug("Metadata produced to destination [" + fileToWrite.getCanonicalPath() + "]");
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }


  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getDestination(), "destination");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {

  }


  public FileNameCreator getFilenameCreator() {
    return filenameCreator;
  }

  /**
   * Set the filename creator implementation used to create the filename.
   *
   * @param creator
   */
  public void setFilenameCreator(FileNameCreator creator) {
    filenameCreator = creator;
  }

  FileNameCreator filenameCreator() {
    return ObjectUtils.defaultIfNull(getFilenameCreator(), new FormattedFilenameCreator());
  }

  public OutputStyle getOutputStyle() {
    return outputStyle;
  }

  /**
   * Set the output style for the metadata.
   *
   * @param style one of Text or XML (default is null, which means Text)
   * @see OutputStyle
   */
  public void setOutputStyle(OutputStyle style) {
    outputStyle = style;
  }

  public MessageDrivenDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination where things are written.
   *
   * @param d the destination.
   */
  public void setDestination(MessageDrivenDestination d) {
    destination = Args.notNull(d, "destination");
  }

  /**
   *
   * @return the overwriteIfExists
   */
  public Boolean getOverwriteIfExists() {
    return overwriteIfExists;
  }

  /**
   * If the file already exists then overwrite it with the current message in transit.
   * <p>
   * In reality, this performs a delete of the file (which fails silently if the file does not exist) prior to attempting to write
   * the file with the payload.
   * </p>
   *
   * @param b true or false (default false).
   */
  public void setOverwriteIfExists(Boolean b) {
    overwriteIfExists = b;
  }

  public boolean overwriteIfExists() {
    return BooleanUtils.toBooleanDefaultIfNull(getOverwriteIfExists(), false);
  }

  private static OutputStyle getStyle(OutputStyle s) {
    return ObjectUtils.defaultIfNull(s, OutputStyle.Text);
  }


  private void validateDir(URL url) throws IOException {
    File f = FsHelper.createFileReference(url);
    if (!f.exists()) {
      log.trace("creating non-existent directoy " + f.getCanonicalPath());
      f.mkdirs();
    }
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  /**
   * Set a metadata filter that will filter out metadata before it is written to filesystem
   *
   * @param filter the filter.
   */
  public void setMetadataFilter(MetadataFilter filter) {
    metadataFilter = filter;
  }

  MetadataFilter metadataFilter() {
    return ObjectUtils.defaultIfNull(getMetadataFilter(), new NoOpMetadataFilter());
  }

  @Override
  public void prepare() throws CoreException {
  }

}
