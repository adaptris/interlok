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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.fs.FsProducer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * File system implementation of <code>AdaptrisMessageProducer</code> with large message support.
 * </p>
 * *
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
 * Additionally the behaviour of this consumer is subtly different from the standard {@link FsProducer} :
 * </p>
 * <ul>
 * <li>This does not honour any use of the Encoder interface as the Encoder interface was designed for small, lightweight files that
 * could be read into memory.</li>
 * <li>The default AdaptrisMessageFactory implementation is {@link FileBackedMessageFactory}</li>
 * <li>If, at runtime, the AdaptrisMessage implementation is not FileBackedMessage, then behaviour changes to be identical to to the
 * existing {@link FsProducer} using the configured FsWorker</li>
 * </ul>
 * 
 * @config large-fs-producer
 * 
 */
@XStreamAlias("large-fs-producer")
@AdapterComponent
@ComponentProfile(summary = "Write the current message to the filesystem with large message support",
    tag = "producer,fs,filesystem")
@DisplayOrder(order = {"createDirs", "filenameCreator", "tempDirectory", "fsWorker"})
public class LargeFsProducer extends FsProducer {

  @AdvancedConfig
  private Boolean useRenameTo;

  public LargeFsProducer() {
    super();
    setMessageFactory(new FileBackedMessageFactory());
  }

  public LargeFsProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }

  private void tryRename(FileBackedMessage msg, File t) throws Exception {
    log.trace("Copying " + msg.currentSource().getCanonicalPath() + " to "
        + t.getCanonicalPath());
    if (useRenameTo()) {
      if (!msg.currentSource().renameTo(t)) {
        copy(msg, t);
      }
    }
    else {
      copy(msg,t);
    }
  }


  private void copy(FileBackedMessage msg, File t) throws Exception {
    File fileToWriteTo = t;
    if (getTempDirectory() != null) {
      File tmpFile = createTempFile(msg);
      // Of course, this tmp file exists, so let's delete it...
      tmpFile.delete();
      log.trace("Writing to temporary file " + tmpFile.getCanonicalPath());
      fileToWriteTo = tmpFile;
    }
    try {
      FileUtils.copyFile(msg.currentSource(), fileToWriteTo);
    }
    catch (IOException e) {
      if (e.getMessage().contains("Failed to copy full contents")) {
        // FileUtils.copyFile uses FileChannel.transferFrom(), which has been
        // causing
        // some problems with NFS mounts, so let's just try again.
        // After all it's a "large file";
        fileToWriteTo.delete();
        FileUtils.copyFile(msg.currentSource(), fileToWriteTo);
      }
      else {
        throw e;
      }
    }
    if (getTempDirectory() != null) {
      log.trace("Renaming temporary file to " + t.getCanonicalPath());
      fileToWriteTo.renameTo(t);
    }
  }


  @Override
  protected void write(AdaptrisMessage msg, File destFile) throws Exception {
    if (msg instanceof FileBackedMessage) {
      tryRename((FileBackedMessage) msg, destFile);
    }
    else {
      super.write(msg, destFile);
    }
  }

  /**
   * @return the useRenameTo value.
   */
  public Boolean getUseRenameTo() {
    return useRenameTo;
  }

  /**
   * Set to true to simply use {@link File#renameTo(File)} when producing an
   * {@link com.adaptris.core.AdaptrisMessage} that is an instance of {@link FileBackedMessage}
   *
   * @param b true to use {@link File#renameTo(File)}
   */
  public void setUseRenameTo(Boolean b) {
    useRenameTo = b;
  }

  boolean useRenameTo() {
    return getUseRenameTo() != null ? getUseRenameTo().booleanValue() : false;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
