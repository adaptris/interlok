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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.fs.FsException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * File system implementation of <code>AdaptrisMessageConsumer</code> with large message support.
 * </p>
 * <p>
 * The configured {@link ConsumeDestination} may return a string in one of two formats
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
 * Additionally the behaviour of this consumer is subtly different from the standard {@link FsConsumer} :
 * </p>
 * <ul>
 * <li>This does not honour any use of the Encoder interface as the Encoder interface was designed for small, lightweight files that
 * could be read into memory.</li>
 * <li>The default AdaptrisMessageFactory implementation is {@link FileBackedMessageFactory}</li>
 * <li>If, at runtime, the MessageFactory implementation is not FileBackedMessageFactory, then behaviour changes to be identical to
 * to the existing {@link FsConsumer} and uses the configured FsWorker</li>
 * </ul>
 * 
 * @config large-fs-consumer
 * 
 */
@XStreamAlias("large-fs-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from the filesystem with large message support", tag = "consumer,fs,filesystem")
public class LargeFsConsumer extends FsConsumer {

  public LargeFsConsumer() {
    setMessageFactory(new FileBackedMessageFactory());
  }

  public LargeFsConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void init() throws CoreException {
    super.init();
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  public void prepareConsumer() throws CoreException {
  }


  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.processFile()", logger = "com.adaptris.perf4j.lms.TimingLogger")
  protected int processFile(File originalFile) throws CoreException {
    int rc = 0;
    try {
      if (originalFile.getName().endsWith(getWipSuffix())) {
        log.debug("ignoring part-processed file [" + originalFile.getName() + "]");
      }
      else {
        if (checkModified(originalFile) && isFileAccessible(originalFile)) {
          AdaptrisMessage msg = defaultIfNull(getMessageFactory()).newMessage();
          File wipFile = renameFile(originalFile);
          addStandardMetadata(msg, originalFile, wipFile);
          if (getMessageFactory() instanceof FileBackedMessageFactory) {
            ((FileBackedMessage) msg).initialiseFrom(wipFile);
          }
          else {
            msg.setPayload(fsWorker.get(wipFile));
          }
          retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
          rc++;
          new FileCleaningTracker().track(wipFile, msg, FileDeleteStrategy.FORCE);
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

}
