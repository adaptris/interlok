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
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
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
public class WriteMetadataToFilesystem extends ServiceImp {

  @NotNull
  @Valid
  @AutoPopulated
  private FileNameCreator fileNameCreator;
  private OutputStyle outputStyle;
  @NotNull
  @Valid
  private ProduceDestination destination;
  private Boolean overwriteIfExists;
  @AutoPopulated
  @NotNull
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
    setFileNameCreator(new FormattedFilenameCreator());
    setMetadataFilter(new NoOpMetadataFilter());
  }

  public WriteMetadataToFilesystem(ProduceDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    OutputStream out = null;
    try {
      String baseUrl = getDestination().getDestination(msg);
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      validateDir(url);
      File fileToWrite = new File(FsHelper.createFileReference(url), getFileNameCreator().createName(msg));
      if (overwriteIfExists()) {
        FileUtils.deleteQuietly(fileToWrite);
      }
      if (fileToWrite.exists()) {
        throw new IOException(fileToWrite.getCanonicalPath() + " already exists");
      }
      out = new FileOutputStream(fileToWrite);
      getStyle(getOutputStyle()).write(getMetadataFilter().filter(msg.getMetadata()), out);
      log.debug("Metadata produced to destination [" + fileToWrite.getCanonicalPath() + "]");
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      closeQuietly(out);
    }
  }


  @Override
  protected void initService() throws CoreException {
    if (getDestination() == null) {
      throw new CoreException("Null Destination");
    }
  }

  @Override
  protected void closeService() {

  }


  public FileNameCreator getFileNameCreator() {
    return fileNameCreator;
  }

  /**
   * Set the filename creator implementation used to create the filename.
   *
   * @param creator
   */
  public void setFileNameCreator(FileNameCreator creator) {
    if (creator == null) {
      throw new IllegalArgumentException("Filename Creator is null");
    }
    fileNameCreator = creator;
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

  public ProduceDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination where things are written.
   *
   * @param d the destination.
   */
  public void setDestination(ProduceDestination d) {
    if (d == null) {
      throw new IllegalArgumentException("Produce Destination is null");
    }
    destination = d;
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
    return getOverwriteIfExists() != null ? getOverwriteIfExists().booleanValue() : false;
  }

  private Collection<MetadataElement> filter(Set<MetadataElement> original) {
    MetadataCollection result = getMetadataFilter().filter(original);
    return result;
  }

  private static OutputStyle getStyle(OutputStyle s) {
    return s != null ? s : OutputStyle.Text;
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
    if (filter == null) {
      throw new IllegalArgumentException("MetadataFilter is null");
    }
    metadataFilter = filter;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
