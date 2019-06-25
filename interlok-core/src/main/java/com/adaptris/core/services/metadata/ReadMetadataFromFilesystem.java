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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
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
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
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
@AdapterComponent
@ComponentProfile(summary = "Read a set of metadata from the filesystem and add/replace current metadata", tag = "service,metadata")
@DisplayOrder(order =
{
    "destination", "inputStyle", "overwriteExistingMetadata", "filenameCreator", "metadataLogger"
})
public class ReadMetadataFromFilesystem extends MetadataServiceImpl {

  private InputStyle inputStyle;
  @NotNull
  @Valid
  private MessageDrivenDestination destination;
  @InputFieldDefault(value = "false")
  private Boolean overwriteExistingMetadata;
  @AdvancedConfig
  @Valid
  private FileNameCreator filenameCreator;

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
  }

  public ReadMetadataFromFilesystem(MessageDrivenDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String filenameToRead = "[could not create filename]";
    try {
      String baseUrl = getDestination().getDestination(msg);
      File parentFile = FsHelper.toFile(baseUrl);
      File fileToRead = new File(parentFile, filenameCreator().createName(msg));
      if (parentFile.isFile()) {
        fileToRead = parentFile;
      }
      filenameToRead = fileToRead.getCanonicalPath();
      log.trace("Reading {}", filenameToRead);
      try (InputStream in = new FileInputStream(fileToRead)) {
        Set<MetadataElement> set = getStyle(getInputStyle()).load(in);
        for (MetadataElement e : set) {
          if (overwriteExistingMetadata() || !msg.headersContainsKey(e.getKey())) {
            msg.addMetadata(e);
          }
        }
        logMetadata("New Metadata for message {}", msg.getMetadata());
      }
    }
    catch (Exception e) {
      log.warn("Failed to read metadata, {} in inaccessible? no changes.", filenameToRead);
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

  public MessageDrivenDestination getDestination() {
    return destination;
  }

  /**
   * Set the destination where things have been written.
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
    return BooleanUtils.toBooleanDefaultIfNull(getOverwriteExistingMetadata(), false);
  }

  private static InputStyle getStyle(InputStyle s) {
    return s != null ? s : InputStyle.Text;
  }

  public FileNameCreator getFilenameCreator() {
    return filenameCreator;
  }

  /**
   * Set the filename creator implementation used to determine the filename to read..
   *
   * @param creator
   */
  public void setFilenameCreator(FileNameCreator creator) {
    filenameCreator = creator;
  }

  FileNameCreator filenameCreator() {
    return getFilenameCreator() != null ? getFilenameCreator() : new FormattedFilenameCreator();
  }

}
