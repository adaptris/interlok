package com.adaptris.core.fs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.perf4j.aop.Profiled;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.fs.FsException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Extension of the standard FsConsumer that traverses all subdirectories for files that match the filter expression.
 * </p>
 * 
 * @config traversing-fs-consumer
 * @license STANDARD
 */
@XStreamAlias("traversing-fs-consumer")
public class TraversingFsConsumer extends FsConsumer {

  public TraversingFsConsumer() {
    super();
  }

  public TraversingFsConsumer(ConsumeDestination d) {
    super(d);
  }

  @Override
  protected int processMessages() {
    List<File> fileList = new ArrayList<File>();
    int filesProcessed = 0;
    try {
      File dir = verifyDirectory();
      fileList = Arrays.asList(dir.listFiles());
    }
    catch (Exception e) {
      log.warn("Exception listing files in [" + getDestination().getDestination() + "], waiting for next scheduled poll");
      if (logAllExceptions()) {
        log.warn(e.getMessage(), e);
      }
      return 0;
    }
    getFileSorter().sort(fileList);
    for (File file : fileList) {
      try {
        filesProcessed += processFile(file);
        if (!continueProcessingMessages()) {
          break;
        }

      }
      catch (Exception e) {
        log.warn("Exception processing [" + file.getName() + "], waiting for next scheduled poll");
        if (logAllExceptions()) {
          log.warn(e.getMessage(), e);
        }
      }
    }
    return filesProcessed;
  }

  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.processFile()", logger = "com.adaptris.perf4j.fs.TimingLogger")
  protected int processFile(File f) throws CoreException {
    int result = 0;
    logFile(f, "processFile ");
    if (f.isDirectory()) {
      result += processDirectory(f);
    }
    else {
      // Can't use super.processFile() here because
      // we need to preserve the "directory name" from which we read the stuff.
      result += handleFile(f);
    }
    return result;
  }

  private int processDirectory(File srcDir) throws CoreException {
    int result = 0;
    logFile(srcDir, "processDirectory ");
    List<File> fileList = Arrays.asList(srcDir.listFiles());
    getFileSorter().sort(fileList);

    for (File f : fileList) {
      result += processFile(f);
    }
    return result;
  }

  private void logFile(File f, String prefix) {
    // try {
    // log.trace(prefix + f.getCanonicalPath());
    // }
    // catch (IOException e) {
    //
    // }

  }
  private int handleFile(File originalFile) throws CoreException {
    int rc = 0;
    logFile(originalFile, "handleFile ");

    try {
      if (originalFile.getName().endsWith(getWipSuffix())) {
        log.debug("ignoring part-processed file [" + originalFile.getName() + "]");
      }
      else if (fileFilter != null && !fileFilter.accept(originalFile)) {
        log.trace("File [" + originalFile.getName() + "] doesn't match filter");
      }
      else {
        if (checkModified(originalFile) && isFileAccessible(originalFile)) {
          File wipFile = renameFile(originalFile);
          AdaptrisMessage msg = createAdaptrisMessage(wipFile);
          addStandardMetadata(msg, originalFile, wipFile);
          retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
          fsWorker.delete(wipFile);
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

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Standard);
  }

}
