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

import static com.adaptris.core.util.MetadataHelper.convertFromProperties;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of {@link com.adaptris.core.Service} that reads metadata from the filesystem.
 * </p>
 * <p>
 * Used in conjunction with {@link WriteMetadataToFilesystem} to allow preservation of metadata across integration points that make
 * use of the filesystem.
 * </p>
 * 
 * @config read-metadata-from-filesystem
 * 
 * 
 * @see WriteMetadataToFilesystem
 */
@XStreamAlias("read-metadata-from-filesystem")
public class ReadMetadataFromFilesystem extends ServiceImp {

  private InputStyle inputStyle;
  @NotNull
  @Valid
  private ProduceDestination destination;
  private Boolean overwriteExistingMetadata;
  @NotNull
  @AutoPopulated
  private FileNameCreator fileNameCreator;

  public enum InputStyle {
    Text {
      @Override
      void load(InputStream in, Properties p) throws IOException {
        p.load(in);
      }
    },
    XML {
      @Override
      void load(InputStream in, Properties p) throws IOException {
        p.loadFromXML(in);
      }
    };

    Set<MetadataElement> load(InputStream in) throws IOException {
      Properties p = new Properties();
      load(in, p);
      return convertFromProperties(p);
    }

    abstract void load(InputStream in, Properties p) throws IOException;
  }

  public ReadMetadataFromFilesystem() {
    super();
    setFileNameCreator(new FormattedFilenameCreator());
  }

  public ReadMetadataFromFilesystem(ProduceDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    InputStream in = null;
    String filenameToRead = "[could not create filename]";

    try {
      String baseUrl = getDestination().getDestination(msg);
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      File fileToRead = new File(FsHelper.createFileReference(url), getFileNameCreator().createName(msg));
      filenameToRead = fileToRead.getCanonicalPath();
      log.trace("Reading " + filenameToRead);
      in = new FileInputStream(fileToRead);
      Set<MetadataElement> set = getStyle(getInputStyle()).load(in);
      for (MetadataElement e : set) {
        if (overwriteExistingMetadata() || !msg.containsKey(e.getKey())) {
          msg.addMetadata(e);
        }
      }
      log.trace("New Metadata for message " + msg.getMetadata());
    }
    catch (Exception e) {
      log.warn("Failed to read metadata, " + filenameToRead + " is inaccessible? no changes.");
    }
    finally {
      closeQuietly(in);
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


  public InputStyle getInputStyle() {
    return inputStyle;
  }

  /**
   * Set the output style for the metadata.
   *
   * @param style one of Text or XML (default is null, which means Text)
   * @see InputStyle
   */
  public void setInputStyle(InputStyle style) {
    inputStyle = style;
  }

  public ProduceDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination where things have been written.
   * <p>
   * Although from a naming perspective it makes no sense to use a 'ProduceDestination' when you are consuming data;
   * {@link ProduceDestination} allows us to derive the destination from the message and lets us keep the configuration largely the
   * same as {@link WriteMetadataToFilesystem}
   * </p>
   *
   * @param d the destination.
   */
  public void setDestination(ProduceDestination d) {
    if (d == null) {
      throw new IllegalArgumentException("Destination is null");
    }
    destination = d;
  }

  /**
   *
   * @return the overwriteIfExists
   */
  public Boolean getOverwriteExistingMetadata() {
    return overwriteExistingMetadata;
  }

  /**
   * Overwrite any existing metadata with the contents of the file.
   *
   * @param b true or false (default false).
   */
  public void setOverwriteExistingMetadata(Boolean b) {
    overwriteExistingMetadata = b;
  }

  public boolean overwriteExistingMetadata() {
    return getOverwriteExistingMetadata() != null ? getOverwriteExistingMetadata().booleanValue() : false;
  }

  private static InputStyle getStyle(InputStyle s) {
    return s != null ? s : InputStyle.Text;
  }

  public FileNameCreator getFileNameCreator() {
    return fileNameCreator;
  }

  /**
   * Set the filename creator implementation used to determine the filename to read..
   *
   * @param creator
   */
  public void setFileNameCreator(FileNameCreator creator) {
    if (creator == null) {
      throw new IllegalArgumentException("Creator is null");
    }
    fileNameCreator = creator;
  }

  @Override
  public void prepare() throws CoreException {
  }


}
