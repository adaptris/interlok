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

package com.adaptris.core.fs;

import static com.adaptris.core.CoreConstants.FS_PRODUCE_DIRECTORY;
import static com.adaptris.core.CoreConstants.PRODUCED_NAME_KEY;
import static com.adaptris.core.util.DestinationHelper.logWarningIfNotNull;
import static com.adaptris.core.util.DestinationHelper.mustHaveEither;
import static com.adaptris.core.util.DestinationHelper.resolveProduceDestination;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * {@link com.adaptris.core.AdaptrisMessageProducer} implementation that writes to the file system.
 *
 * @config fs-producer
 *
 */
@XStreamAlias("fs-producer")
@AdapterComponent
@ComponentProfile(summary = "Write the current message to the filesystem", tag = "producer,fs,filesystem",
    recommended =
    {
        NullConnection.class
    }, metadata =
    {
        "producedname", "fsProduceDir"
    })
@DisplayOrder(
    order = {"baseDirectoryUrl", "createDirs", "filenameCreator", "tempDirectory", "fsWorker"})
public class FsProducer extends ProduceOnlyProducerImp {

  /**
   * Specify whether to create directories that do not exist.
   * <p>
   * If this flag has been set, then an attempt to create the directory is made, if the directory
   * does not exist. The default is {@code false} if not explicitly configured.
   * </p>
   */
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean createDirs;
  /**
   * The temporary directory for initially writing files to.
   * <p>
   * In some instances, for instance, writing to network shares, it may be preferable to initial
   * write to a temporary directory and then move the resulting file to the final location. Setting
   * this to a non-null value will cause a temporary file to be created in this directory; this is
   * then renamed to the correct location using {@link File#renameTo(File)}.
   * </p>
   */
  @AdvancedConfig
  @Getter
  @Setter
  private String tempDirectory = null;
  /**
   * The {@link FsWorker} implementation to use when performing write operations.
   * <p>
   * The default is {@link NioWorker} if not explicitly configured
   * </p>
   */
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  @NonNull
  @Getter
  @Setter
  private FsWorker fsWorker;
  /**
   * Sets the {@code FileNameCreator}.
   * <p>
   * The default is {@link FormattedFilenameCreator} if not explicitly configured
   * </p>
   */
  @NotNull
  @Valid
  @AutoPopulated
  @NonNull
  @Getter
  @Setter
  private FileNameCreator filenameCreator;
  /**
   * The consume destination represents the base-directory where you are consuming files from.
   *
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @Removal(version = "4.0.0", message = "Use 'base-directory-url' instead")
  private ProduceDestination destination;

  /**
   * The base directory specified as a URL.
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String baseDirectoryUrl;

  private transient boolean destWarning;

  public FsProducer() {
    setFilenameCreator(new FormattedFilenameCreator());
    setFsWorker(new NioWorker());
  }

  private FileNameCreator filenameCreatorToUse() {
    return getFilenameCreator();
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String baseUrl) throws ProduceException {
    FileNameCreator creator = filenameCreatorToUse();
    try {
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      validateDir(url);
      File filetoWrite = new File(FsHelper.createFileReference(url), creator.createName(msg));
      addProducerMetadata(msg, filetoWrite);
      write(msg, filetoWrite);
      log.debug("msg produced to destination [{}]", url);
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
  }

  protected void addProducerMetadata(AdaptrisMessage msg, File destFile) throws IOException {
    File canonicalFile = destFile.getCanonicalFile();
    msg.addMetadata(PRODUCED_NAME_KEY, canonicalFile.getName());
    if (canonicalFile.getParentFile() != null) {
      msg.addMetadata(FS_PRODUCE_DIRECTORY, canonicalFile.getParentFile().getCanonicalPath());
    }
  }

  protected void write(AdaptrisMessage msg, File destFile) throws Exception {
    File fileToWriteTo = destFile;

    if (getTempDirectory() != null) {
      File tmpFile = createTempFile(msg);
      log.trace("Writing to temporary file " + tmpFile.getCanonicalPath());
      fileToWriteTo = tmpFile;
    }
    fsWorker.put(encode(msg), fileToWriteTo);
    if (getTempDirectory() != null) {
      log.trace("Renaming temporary file to " + destFile.getCanonicalPath());
      fileToWriteTo.renameTo(destFile);
    }
  }

  protected File createTempFile(AdaptrisMessage msg) throws Exception {
    URL tmpDirUrl = FsHelper.createUrlFromString(getTempDirectory(), true);
    validateDir(tmpDirUrl);
    File tmpDir = FsHelper.createFileReference(tmpDirUrl);
    File tmpFile = File.createTempFile(msg.getUniqueId() + "-", null, tmpDir);
    // Of course, this tmp file exists, so let's delete it...
    tmpFile.delete();
    return tmpFile;
  }

  protected void validateDir(URL url) throws IOException {
    if (shouldCreateDirs()) {
      File f = FsHelper.createFileReference(url);
      if (!f.exists()) {
        log.trace("creating non-existent directory " + f.getCanonicalPath());
        f.mkdirs();
      }
    }
  }

  protected boolean shouldCreateDirs() {
    return BooleanUtils.toBooleanDefaultIfNull(getCreateDirs(), false);
  }


  @Override
  public void prepare() throws CoreException {
    logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'base-directory-url' instead", LoggingHelper.friendlyName(this));
    mustHaveEither(getBaseDirectoryUrl(), getDestination());
    registerEncoderMessageFactory();
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return resolveProduceDestination(getBaseDirectoryUrl(), getDestination(), msg);
  }

  @SuppressWarnings("unchecked")
  public <T extends FsProducer> T withBaseDirectoryUrl(String s) {
    setBaseDirectoryUrl(s);
    return (T) this;
  }
}
