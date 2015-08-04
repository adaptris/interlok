package com.adaptris.core.fs;

import static com.adaptris.core.CoreConstants.FS_PRODUCE_DIRECTORY;
import static com.adaptris.core.CoreConstants.PRODUCED_NAME_KEY;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link AdaptrisMessageProducer} implementation that writes to the file system.
 * 
 * @config fs-producer
 * @license BASIC
 */
@XStreamAlias("fs-producer")
public class FsProducer extends ProduceOnlyProducerImp {

  private Boolean createDirs;
  @AdvancedConfig
  private String tempDirectory = null;
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  private FsWorker fsWorker;
  @NotNull
  @Valid
  @AutoPopulated
  private FileNameCreator filenameCreator;

  public FsProducer() {
    setFilenameCreator(new FormattedFilenameCreator());
    setFsWorker(new NioWorker());
  }

  public FsProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic);
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
    // na
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
    // na
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
    // na
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer #produce(AdaptrisMessage, ProduceDestination)
   */
  @Override
  @Profiled(tag = "{$this.getClass().getSimpleName()}.produce()", logger = "com.adaptris.perf4j.fs.TimingLogger")
  public void produce(AdaptrisMessage msg, ProduceDestination overload) throws ProduceException {

    if (overload != null) {
      try {
        doProduce(msg, overload.getDestination(msg));
      }
      catch (CoreException e) {
        throw new ProduceException(e);
      }
    }
    else {
      throw new ProduceException("ProduceDestination is null");
    }
  }

  private FileNameCreator filenameCreatorToUse() {
    return getFilenameCreator();
  }

  protected void doProduce(AdaptrisMessage msg, String baseUrl) throws ProduceException {
    FileNameCreator creator = filenameCreatorToUse();
    try {
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      validateDir(url);
      File filetoWrite = new File(FsHelper.createFileReference(url), creator.createName(msg));
      addProducerMetadata(msg, filetoWrite);
      write(msg, filetoWrite);
      log.debug("msg produced to destination [" + url + "]");
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

  /**
   * Specify whether to create directories that do not exist.
   * <p>
   * When the ProduceDestination returns a destination, if this flag has been set, then an attempt to create the directory is made,
   * if the directory does not exist.
   *
   * @param b true to enable directory creation, default false
   */
  public void setCreateDirs(Boolean b) {
    createDirs = b;
  }

  /**
   * Get the flag specifying creation of directories as required..
   *
   * @return true or false, default null which means false
   * @see #shouldCreateDirs()
   */
  public Boolean getCreateDirs() {
    return createDirs;
  }


  public boolean shouldCreateDirs() {
    return getCreateDirs() != null ? getCreateDirs().booleanValue() : false;
  }

  /**
   * @return the tempDirectory
   */
  public String getTempDirectory() {
    return tempDirectory;
  }

  /**
   * Set the temporary directory for initially writing files to.
   * <p>
   * In some instances, for instance, writing to network shares, it may be preferable to initial write to a temporary directory and
   * then move the resulting file to the final location. Setting this to a non-null value will cause a temporary file to be created
   * in this directory; this is then renamed to the correct location using {@link File#renameTo(File)}.
   * </p>
   *
   * @param s the tempDirectory to set
   */
  public void setTempDirectory(String s) {
    tempDirectory = s;
  }

  /**
   * Get the {@link FsWorker} implementation.
   *
   * @return the {@link FsWorker} implementation.
   */
  public FsWorker getFsWorker() {
    return fsWorker;
  }

  /**
   * Set the {@link FsWorker} implementation to use when performing write operations.
   *
   * @param fw the fsWorker implementation, default {@link NioWorker}
   */
  public void setFsWorker(FsWorker fw) {
    if (fw == null) {
      throw new IllegalArgumentException("fsWorker may not be null");
    }
    fsWorker = fw;
  }

  public FileNameCreator getFilenameCreator() {
    return filenameCreator;
  }

  /**
   * <p>
   * Sets the <code>FileNameCreator</code> to use. May not be null.
   * </p>
   *
   * @param creator the {@link FileNameCreator} to use, default is {@link FormattedFilenameCreator}
   */
  public void setFilenameCreator(FileNameCreator creator) {
    if (creator == null) {
      throw new IllegalArgumentException("FileNameCreator may not be null");
    }
    filenameCreator = creator;
  }
}
