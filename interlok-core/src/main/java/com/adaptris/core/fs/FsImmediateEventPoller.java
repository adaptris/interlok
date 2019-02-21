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

import static com.adaptris.core.fs.FsHelper.createFileReference;
import static com.adaptris.core.fs.FsHelper.createUrlFromString;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.core.PollerImp;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>Poller</code> which listens for file events. Whenever a file is created this even based poller, will be
 * triggered immediately.
 * </p>
 * <p>
 * Note; This poller implementation should only be used with file system Consumers. And you must configure a (> 0) quiet-period for
 * your fs consumer. The quiet period is used to check that the file has completed it's creation.
 * </p>
 * <p>
 * This poller will only trigger the consumer once a file has completed it's creation. The reason for this is that you may have a
 * big file that starts to be created, this poller will be immediately invoked. If we were to hand over to the consumer at this
 * point (while the file is still copying) the consumer has it's own check to make sure the file has been created in full and
 * therefore not process the file. Instead it assumes there will be another poll shortly. However, if more files are not added to
 * our "in" directory for a while, this event will not happen until more files are added. In these situations we could end up not
 * processing files until the next file is added.
 * </p>
 * 
 * @config fs-immediate-event-poller
 * 
 */
@XStreamAlias("fs-immediate-event-poller")
public class FsImmediateEventPoller extends PollerImp {

  private static final long DEFAULT_CREATION_COMPLETE_CHECK_MS = 500;
  
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private TimeInterval creationCompleteCheck;

  private transient boolean stopped = false;
  private transient Thread monitorThread;
  
  public FsImmediateEventPoller() {
  }
  

  @Override
  public void prepare() throws CoreException {}

  @Override
  public void init() throws CoreException {
    if(!(this.retrieveConsumer() instanceof FsConsumer))
      throw new CoreException("You cannot configure a file system event poller with any non file system consumer.");
    else {
      if(((FsConsumer)this.retrieveConsumer()).getQuietInterval() == null)
        throw new CoreException("With the immediate file system poller, you must configure a quiet period for the FsConsumer");
    }
      
    monitorThread = createThread();
  }


  @Override
  public void start() throws CoreException {
    stopped = false;
    monitorThread.start();
  }

  @Override
  public void stop() {
    stopped = true;
    if(monitorThread != null)
      monitorThread.interrupt();
  }

  @Override
  public void close() {

  }

  private Thread createThread() {
    return new Thread("FsImmediateEventPollerThread") {
      
      @SuppressWarnings("unchecked")
      public void run() {
        try {
          WatchService watcher = FileSystems.getDefault().newWatchService();
          File directory = createFileReference(createUrlFromString(retrieveConsumer().getDestination().getDestination(), true));
          Path dir = Paths.get(directory.getCanonicalPath());
          
          dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

          FsConsumer consumer = (FsConsumer) retrieveConsumer();
          String wipSuffix = consumer.getWipSuffix();
            
          boolean processMessages;

          // Incase there are files in the "in" directory before we start watching for file events.
          processMessages();
          
          while(!stopped) {
            processMessages = false;
            
            WatchKey key;
            try {
              key = watcher.take();
            } catch (InterruptedException x) {
              return;
            }
            
            for (WatchEvent<?> event : key.pollEvents()) {
              WatchEvent.Kind<?> kind = event.kind();
      
              if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path filename = ev.context();
                                
                if(!filename.toString().endsWith(wipSuffix)) { // e don't want to be triggered for WIP files.
                  this.waitUntilCompleted(new File(directory, filename.toString()), consumer.getQuietInterval());
                  processMessages = true;
                }
              }
              else 
                continue;
            }
            
            if(processMessages)
              processMessages();
            
            boolean valid = key.reset();
            if (!valid)
              break;
            
          }
        }
        catch (InterruptedException ex) {
          return;
        }
        catch (Exception x) {
          log.error("Caught Exception {}", x.getMessage());
        }
      }

      // Wait for the specified time interval when the file is no longer modified.
      // This is protection against creation events that have not yet completed.
      private void waitUntilCompleted(File file, TimeInterval quietInterval) throws InterruptedException {
        long quietPeriod = quietInterval.toMilliseconds();
        
        long now = System.currentTimeMillis();
        long lastModified = file.lastModified();
        
        while (!(now - lastModified >= quietPeriod)) {
          Thread.sleep(createCompleteCheckMs());
          lastModified = file.lastModified();
        }
      }
      
    };
  }

  long createCompleteCheckMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getCreationCompleteCheck(),
        DEFAULT_CREATION_COMPLETE_CHECK_MS);
  }

  public TimeInterval getCreationCompleteCheck() {
    return creationCompleteCheck;
  }


  /**
   * Specify the wait time between checking for file creation.
   * 
   * @param t the time interval; default is 500ms.
   */
  public void setCreationCompleteCheck(TimeInterval t) {
    this.creationCompleteCheck = t;
  }
}
