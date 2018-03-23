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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.util.Args;
import com.adaptris.fs.FsException;
import com.adaptris.fs.FsFilenameExistsException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * File system implementation of <code>AdaptrisMessageConsumer</code> based on the <code>com.adaptris.fs</code> package.
 * </p>
 * <p>
 * The configured <code>Destination</code> may return a string in one of two formats
 * </p>
 * <ul>
 * <li>If a <code>file</code> based url is used. e.g. file:///c:/path/to/my/directory or file:////path/to/my/directory then the
 * patch is considered to be fully qualified</li>
 * <li>If just a path is returned, then it is considered to be relative to the current working directory. e.g. if /opt/fred is used,
 * and the adapter is installed to /opt/adapter, then the fully qualified name is /opt/adapter/opt/fred.</li>
 * </ul>
 * <p>
 * On windows based platforms, you should always use a file based url.
 * </p>
 * <p>
 * Once A file has been consumed from the file-system a standard set of metadata is added to the resulting message;
 * <table>
 * <th>Metadata Key</th>
 * <th>Description</th>
 * <tr>
 *   <td>originalname</td>
 *   <td>Metadata key for storing the original name (generally file name) of a message. </td>
 * </tr>
 * <tr>
 *   <td>lastmodified</td>
 *   <td>Metadata key for storing the last modified date of the consumed file. </td>
 * </tr>
 * <tr>
 *   <td>fsFileSize</td>
 *   <td>Metadata key for storing the size of the message.</td>
 * </tr>
 * <tr>
 *   <td>fsConsumeDir</td>
 *   <td>Metadata key for storing the directory where a file was consumed from. </td>
 * </tr>
 * <tr>
 *   <td>fsParentDir</td>
 *   <td>Metadata key for storing the name of the immediate parent directory that a file was consumed from. </td>
 * </tr>
 * </table>
 * @config fs-consumer
 * 
 */
@XStreamAlias("fs-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from the filesystem", tag = "consumer,fs,filesystem",
    metadata =
    {
        "originalname", "lastmodified", "fsFileSize", "fsConsumeDir", "fsParentDir"
    }, recommended =
    {
        NullConnection.class
    })
@DisplayOrder(order = {"poller", "createDirs", "fileFilterImp", "fileSorter", "wipSuffix", "resetWipFiles"})
public class FsConsumer extends FsConsumerImpl {

  @NotBlank
  @AutoPopulated
  @AdvancedConfig
  private String wipSuffix;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean resetWipFiles;

  /**
   * <p>
   * Creates a new instance. Defaults to <code>NioWorker</code> and a work in progress suffix of <code>.wip</code>.
   * </p>
   */
  public FsConsumer() {
    super();
    setWipSuffix(".wip");
  }

  public FsConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
  }

  @Override
  protected int processFile(File originalFile) throws CoreException {
    int rc = 0;
    try {
      if (originalFile.getName().endsWith(wipSuffix)) {
        log.debug("ignoring part-processed file [" + originalFile.getName() + "]");

      }
      else {
        if (checkModified(originalFile) && isFileAccessible(originalFile)) {
          File fileToProcess = renameFile(originalFile);
          AdaptrisMessage msg = createAdaptrisMessage(fileToProcess);
          addStandardMetadata(msg, originalFile, fileToProcess);
          retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
          fsWorker.delete(fileToProcess);
          rc++;
        }
        else {
          log.trace(originalFile.getName() + " not deemed safe to process");
        }
      }
    }
    catch (FsException e) {
      throw new CoreException(e);
    }
    catch (IOException e) {
      throw new CoreException(e);
    }
    return rc;
  }

  protected File renameFile(File file) throws FsException {
    File newFile = new File(file.getAbsolutePath() + wipSuffix);

    try {
      fsWorker.rename(file, newFile);
    }
    catch (FsFilenameExistsException e) {
      newFile = new File(file.getParentFile(), System.currentTimeMillis() + "." + file.getName() + wipSuffix);
      fsWorker.rename(file, newFile);
    }
    return newFile;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    try {
      if (resetWipFiles()) {
        renameWipFiles();
      }
    }
    catch (Exception e) {
      if (CoreException.class.isAssignableFrom(e.getClass())) {
        throw (CoreException) e;
      }
      else {
        throw new CoreException(e);
      }
    }
    super.init();
  }

  private void renameWipFiles() throws Exception {
    URL urlDestination = FsHelper.createUrlFromString(getDestination().getDestination(), true);
    File dir = FsHelper.createFileReference(urlDestination);
    if (!dir.exists()) {
      // No point because it doesn't exist.
      return;
    }
    RegexFileFilter p5 = new RegexFileFilter(".*\\" + getWipSuffix());
    File[] files = dir.listFiles((FilenameFilter) p5);
    if (files == null) {
      throw new CoreException("Failed to list files in " + dir.getCanonicalPath()
          + ", incorrect permissions?. Cannot reset WIP files");
    }
    for (File f : files) {
      File parent = f.getParentFile();
      String name = f.getName().replaceAll(getWipSuffix().replaceAll("\\.", "\\\\."), "");
      log.trace("Will Rename " + f.getName() + " back to " + name);
      File newFile = new File(parent, name);
      f.renameTo(newFile);
    }
  }

  /**
   * <p>
   * Sets the work-in-progress suffix to use. May not be null or empty. This suffix is added to the original file name while the
   * file is being processed.
   * </p>
   *
   * @param s the work-in-progress suffix to use
   */
  public void setWipSuffix(String s) {
    wipSuffix = Args.notBlank(s, "wip suffix");
  }

  /**
   * <p>
   * Returns the work-in-progress suffix being used.
   * </p>
   *
   * @return the work-in-progress suffix being used
   */
  public String getWipSuffix() {
    return wipSuffix;
  }

  /**
   * @return the ResetWipFiles
   */
  public Boolean getResetWipFiles() {
    return resetWipFiles;
  }

  /**
   * Specify whether to rename files that are deemed to be in progress back to their original extension upon initialisation.
   * <p>
   * <strong>Note that if the workfile has been created with the current time in ms (due to file-conflicts upon a previous
   * execution), then setting this to true will result in the current time in ms still being present and may not result in the
   * re-processing of the files</strong>.
   * </p>
   *
   * @param b the renameWorkFilesUponInitialisation to set
   */
  public void setResetWipFiles(Boolean b) {
    resetWipFiles = b;
  }

  protected boolean resetWipFiles() {
    return resetWipFiles != null ? resetWipFiles.booleanValue() : false;
  }

  @Override
  protected void prepareConsumer() throws CoreException {
  }
}
