package com.adaptris.core.ftp;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.FileFilter;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.CompositeFileFilter;
import com.adaptris.core.fs.SizeGreaterThan;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * FTP implementation which does not have any guarantees as to the atomicity of operations.
 * <p>
 * This differs from the standard {@link FtpConsumer} in that it does not attempt to rename the file before attempting to process
 * the file. If you have multiple consumers reading the same directory with the same filter then it will be possible to process a
 * message twice (or twice partially) or any combination thereof. <strong>It is not recommended that you use this FtpConsumer unless
 * there are very specific reasons to; e.g. the remote FTP server does not support the RNFR and RNTO command.</strong> After
 * consuming the file, it is deleted.
 * </p>
 * <p>
 * The destination returned by the ConsumeDestination implementation should be in the form in the URL form dictated by the
 * {@link FileTransferConnection} flavour including the directory from which you wish to consume from. Simply specifying the IP
 * Address or DNS name of the remote ftp server may cause files to be consumed without specifying a subdirectory (which if you are
 * not in an ftp chroot jail might be a very bad thing).
 * </p>
 * 
 * @config relaxed-ftp-consumer
 * @license BASIC
 * @see FtpConnection
 * @see SftpConnection
 * @see FileTransferConnection
 * @see com.adaptris.core.ConsumeDestination
 */
@XStreamAlias("relaxed-ftp-consumer")
public class RelaxedFtpConsumer extends AdaptrisPollingConsumer {
  private static final TimeInterval DEFAULT_OLDER_THAN = new TimeInterval(0L, TimeUnit.MILLISECONDS);
  private static final String FORWARD_SLASH = "/";
  private static final String BACK_SLASH = "\\";

  @AdvancedConfig
  private String fileFilterImp;
  @AdvancedConfig
  private TimeInterval olderThan;
  @AdvancedConfig
  private Boolean failOnDeleteFailure;

  private transient FileFilter fileFilter;
  private transient boolean additionalDebug = false;
  private transient FileTransferClient ftpClient = null;

  /**
   * Default Constructor with the following defaults.
   * <ul>
   * <li>reacquireLockBetweenMessages is true</li>
   * <li>fileFilterImp is org.apache.oro.io.GlobFilenameFilter</li>
   * </ul>
   */
  public RelaxedFtpConsumer() {
    setReacquireLockBetweenMessages(true);
    setFileFilterImp("org.apache.oro.io.GlobFilenameFilter");
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    initFileFilter();
    super.init();
  }

  /**
   * <p>
   * NB the <code>String</code> expression that is used to filter messages is obtained from <code>ConsumeDestination</code>.
   * </p>
   */
  private void initFileFilter() throws CoreException {
    String filterExp = getDestination().getFilterExpression();
    try {
      if (filterExp != null) {
        Class[] paramTypes =
        {
          filterExp.getClass()
        };
        Object[] args =
        {
          filterExp
        };

        Class c = Class.forName(fileFilterImp);
        Constructor cnst = c.getDeclaredConstructor(paramTypes);

        fileFilter = (FileFilter) cnst.newInstance(args);
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisPollingConsumer#processMessages()
   */
  @Override
  protected int processMessages() {
    int count = 0;
    String pollDirectory;
    String procDir = null;
    FileTransferConnection con = retrieveConnection(FileTransferConnection.class);
    additionalDebug = con.additionalDebug();
    try {
      ftpClient = con.connect(getDestination().getDestination());
      pollDirectory = con.getDirectoryRoot(getDestination().getDestination());
    }
    catch (Exception e) {
      log.error("Failed to connect to " + getDestination().getDestination(), e);
      return 0;
    }
    try {
      if (additionalDebug) {
        log.trace("Polling " + pollDirectory);
      }
      String[] files = null;
      if (fileFilter != null) {
        files = ftpClient.dir(pollDirectory, fileFilter);
      }
      else {
        files = ftpClient.dir(pollDirectory);
      }
      if (additionalDebug) {
        log.trace("There are potentially [" + files.length + "] messages to process");
      }
      for (int i = 0; i < files.length; i++) {
        try {
          count += processMessage(pollDirectory + FORWARD_SLASH + getUnqualifiedFilename(files[i])) ? 1 : 0;
        }
        catch (Exception e) {
          log.error("Error processing " + pollDirectory + FORWARD_SLASH + files[i] + " from remote host", e);
        }
        if (!continueProcessingMessages()) {
          break;
        }
      }
    }
    catch (Exception e) {
      log.warn("Failed to poll [" + pollDirectory + "] hoping for success next poll time");
    }
    finally {
      con.disconnect(ftpClient);
      ftpClient = null;
    }
    return count;
  }

  private String getUnqualifiedFilename(String s) {
    String result = s;
    int slashPos = -1;
    if (retrieveConnection(FileTransferConnection.class).windowsWorkaround()) {
      slashPos = s.lastIndexOf(BACK_SLASH);
    }
    else {
      slashPos = s.lastIndexOf(FORWARD_SLASH);
    }
    if (slashPos >= 0) {
      result = s.substring(slashPos + 1);
    }
    return result;
  }

  private boolean processMessage(String fullPath) throws Exception {
    long olderThanMs = olderThanMs();
    if (olderThanMs > 0) {
      long now = System.currentTimeMillis();
      long lastModified = ftpClient.lastModified(fullPath);
      if (!(now - lastModified >= olderThanMs)) {
        log.trace("[" + fullPath + "] not deemed safe to process, " + "lastModified on server=[" + new Date(lastModified)
            + "];file must be older than=[" + new Date(now - olderThanMs) + "]");
        return false;
      }
    }
    String filename = fullPath;
    int pos = fullPath.lastIndexOf(FORWARD_SLASH);
    if (pos >= 0 && fullPath.length() > pos) {
      filename = fullPath.substring(pos + 1);
    }
    if (additionalDebug) {
      log.trace("Start processing [" + fullPath + "]");
    }
    AdaptrisMessage adpMsg = null;
    if (getEncoder() == null) {
      adpMsg = defaultIfNull(getMessageFactory()).newMessage();
      OutputStream out = adpMsg.getOutputStream();
      ftpClient.get(out, fullPath);
      out.close();
    }
    else {
      byte[] remoteBytes = ftpClient.get(fullPath);
      adpMsg = decode(remoteBytes);
    }
    adpMsg.addMetadata(CoreConstants.ORIGINAL_NAME_KEY, filename);
    adpMsg.addMetadata(CoreConstants.FS_FILE_SIZE, "" + adpMsg.getSize());
    retrieveAdaptrisMessageListener().onAdaptrisMessage(adpMsg);
    try {
      ftpClient.delete(fullPath);
    }
    catch (Exception e) {
      if (failOnDeleteFailure()) {
        throw e;
      }
    }
    return true;
  }

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Basic);
  }

  /**
   * @return Returns the fileFilterImp.
   */
  public String getFileFilterImp() {
    return fileFilterImp;
  }

  /**
   * Set the filename filter implementation that will be used for filtering files.
   * <p>
   * The <code>String</code> expression that is used to filter messages is obtained from <code>ConsumeDestination</code>.
   * </p>
   * <p>
   * Note that because we working against a remote server, support for additional file attributes such as size (e.g. via
   * {@link SizeGreaterThan}) or last modified may not be supported. Stick to filtering by filename only
   * </p>
   *
   * @param s The fileFilterImp to set.
   * @see com.adaptris.core.ConsumeDestination#getFilterExpression()
   */
  public void setFileFilterImp(String s) {
    fileFilterImp = s;
  }

  long olderThanMs() {
    return getOlderThan() != null ? getOlderThan().toMilliseconds() : DEFAULT_OLDER_THAN.toMilliseconds();
  }

  public TimeInterval getOlderThan() {
    return olderThan;
  }

  /**
   * Specify the period before a file is deemed safe to be processed.
   * <p>
   * The purpose of this is to delay processing of files that may be currently being written to by another process. On certain
   * platforms (e.g. most Unix) it is still possible to obtain an exclusive lock on the file even though it is being written to by
   * another process.
   * </p>
   * <p>
   * <strong>Note: your mileage may vary when using this setting. The FTP Server and the FTP Client will almost certainly have to
   * time-synchronized. Depending on the FTP Server implementation in question, you may need to additionally specify the server's
   * timezone in order to get accurate information.</strong>Additionally, the remote FTP server needs to support support the MDTM
   * command.
   * </p>
   * 
   * @param interval the quietPeriod to set (default to 0)
   * @see FtpConnection#setServerTimezone(String)
   * @see CompositeFileFilter
   * @see #setFileFilterImp(String)
   */
  public void setOlderThan(TimeInterval interval) {
    olderThan = interval;
  }

  private boolean failOnDeleteFailure() {
    return getFailOnDeleteFailure() != null ? getFailOnDeleteFailure().booleanValue() : false;
  }

  /**
   * @return the failOnDeleteFailure
   */
  public Boolean getFailOnDeleteFailure() {
    return failOnDeleteFailure;
  }

  /**
   * Whether or not an attempt to delete the file after processing should result in an exception if it fails.
   * <p>
   * By the time the delete attempt has been made; the file has been processed by the adapter. If the delete fails (for whatever
   * reason), then it will still be possible for the adapter to re-process the file again if it exists upon the next poll trigger.
   * Setting it to be true simply allows you to record an error in the adapter log file.
   * </p>
   *
   * @param b the failOnDeleteFailure to set (default false)
   */
  public void setFailOnDeleteFailure(Boolean b) {
    failOnDeleteFailure = b;
  }
}
