package com.adaptris.filetransfer;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.adaptris.ftp.FtpException;

/**
 * Common interface for all FTP client flavours.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public interface FileTransferClient {

  /**
   * Connect and login into an account on the FTP server. This completes the entire login process
   * 
   * @param user user name
   * @param password user's password
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void connect(String user, String password) throws IOException,
      FileTransferException;

  /**
   * Put a local file onto the FTP server.
   *
   * @param localPath path of the local file
   * @param remoteFile name of remote file in current directory
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void put(String localPath, String remoteFile) throws IOException,
      FileTransferException;

  /**
   * Put a stream of data onto the FTP server.
   *
   * @param srcStream input stream of data to put
   * @param remoteFile name of remote file
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void put(InputStream srcStream, String remoteFile) throws IOException,
      FileTransferException;

  /**
   * Put a local file onto the FTP server.
   *
   * @param localPath path of the local file
   * @param remoteFile name of remote file
   * @param append true if appending, false otherwise
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void put(String localPath, String remoteFile, boolean append)
      throws IOException, FileTransferException;

  /**
   * Put a stream of data onto the FTP server.
   *
   * @param srcStream input stream of data to put
   * @param remoteFile name of remote file
   * @param append true if appending, false otherwise
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void put(InputStream srcStream, String remoteFile, boolean append)
      throws IOException, FileTransferException;

  /**
   * Put data onto the FTP server.
   *
   * @param bytes array of bytes
   * @param remoteFile name of remote file
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void put(byte[] bytes, String remoteFile) throws IOException,
      FileTransferException;

  /**
   * Put data onto the FTP server.
   *
   * @param bytes array of bytes
   * @param remoteFile name of remote file
   * @param append true if appending, false otherwise
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void put(byte[] bytes, String remoteFile, boolean append) throws IOException,
      FileTransferException;

  /**
   * Get data from the FTP server.
   *
   * @param localPath local file to put data in
   * @param remoteFile name of remote file
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void get(String localPath, String remoteFile) throws IOException,
      FileTransferException;

  /**
   * Get data from the FTP server.
   *
   * @param destStream data stream to write data to
   * @param remoteFile name of remote file
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void get(OutputStream destStream, String remoteFile) throws IOException,
      FileTransferException;

  /**
   * Get data from the FTP server.
   *
   * @param remoteFile name of remote file
   * @return a byte array.
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  byte[] get(String remoteFile) throws IOException, FileTransferException;

  /**
   * List current directory's contents as an array of strings of filenames.
   *
   * @return an array of current directory listing strings
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  String[] dir() throws IOException, FileTransferException;

  /**
   * List a directory's contents as an array of strings of filenames.
   *
   * @param dirname name of directory(<b>not</b> a file mask)
   * @return an array of directory listing strings
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  String[] dir(String dirname) throws IOException, FileTransferException;

  /**
   * List a directory's contents as an array of strings.
   * <p>
   * A detailed listing is available, otherwise just filenames are provided. The
   * detailed listing varies in details depending on OS and FTP server. Note
   * that a full listing can be used on a file name to obtain information about
   * a file. The special files "." and ".." are ignored.
   * </p>
   *
   * @param dirname name of directory (<b>not</b> a file mask)
   * @param full true if detailed listing required false otherwise
   * @return an array of directory listing strings
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  String[] dir(String dirname, boolean full) throws IOException,
      FileTransferException;

  /**
   * List a directory's contents
   *
   * @param directory the directory to list.
   * @param filter the filefilter mask to use
   * @return an array of strings containing the listing
   * @throws IOException on comms error.
   * @throws FileTransferException on FTP Specific exception error.
   */
  String[] dir(String directory, FileFilter filter)
      throws FileTransferException, IOException;

  /**
   * List a directory's contents
   *
   * @param directory the directory to list.
   * @param filter the filefilter mask to use
   * @return an array of strings containing the listing
   * @throws IOException on comms error.
   * @throws FileTransferException on FTP Specific exception error.
   * @deprecated use {#link dir(String, FileFilter)} instead.
   */
  @Deprecated
  String[] dir(String directory, FilenameFilter filter)
      throws FileTransferException, IOException;

  /**
   * Delete the specified remote file
   *
   * @param remoteFile name of remote file to delete
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void delete(String remoteFile) throws IOException, FileTransferException;

  /**
   * Rename a file or directory
   *
   * @param from name of file or directory to rename
   * @param to intended name
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void rename(String from, String to) throws IOException, FileTransferException;

  /**
   * Delete the specified remote working directory
   *
   * @param dir name of remote directory to delete
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void rmdir(String dir) throws IOException, FileTransferException;

  /**
   * Create the specified remote working directory
   *
   * @param dir name of remote directory to create
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void mkdir(String dir) throws IOException, FileTransferException;

  /**
   * Change the remote working directory to that supplied
   *
   * @param dir name of remote directory to change to
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void chdir(String dir) throws IOException, FileTransferException;

  /**
   * Quit the FTP session
   *
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  void disconnect() throws IOException, FileTransferException;

  /**
   * Return the last modified time for the given path.
   *
   * @param path the path to the filename.
   * @return the last modified time
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  long lastModified(String path) throws IOException, FileTransferException;

  /**
   * Return the last modified date for the given path.
   *
   * @param path the path to the filename.
   * @return the date.
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  Date lastModifiedDate(String path) throws IOException, FileTransferException;

  /**
   * Get the time to wait between sending control connection keepalive messages.
   *
   * @return time in seconds
   */
  long getKeepAliveTimeout() throws FtpException;

  /**
   * Set the time to wait between sending control connection keepalive messages when processing file upload or download.
   *
   * @param seconds time in seconds
   */
  void setKeepAliveTimeout(long seconds) throws FtpException;


  /**
   * Check if this client is still connected to its target
   * Any errors return a false
   * This is used to check cached connections are still working
   *
   * @return true = connected, false = not connected or some error
   */
  boolean isConnected();

  /**
   * Switch additional debug on or off
   * 
   * @param b set to true if you wish to get some additional debugging in the log file, defaults to
   *        false
   */
  public void setAdditionalDebug(boolean b);

}