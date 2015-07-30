/*
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003  Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 *  $Id: FtpClient.java,v 1.7 2009/07/08 13:51:47 lchan Exp $
 */

package com.adaptris.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferClientImp;
import com.adaptris.filetransfer.FileTransferException;

/**
 * Supports client-side FTP. Most common FTP operations are present in this class.
 * 
 * @author Bruce Blackshaw
 * @author $Author: lchan $
 * @deprecated since 2.8.2, the core producer and consumer implementations use {@link CommonsNetFtpClient} instead.
 */
@Deprecated
public class FtpClient extends FileTransferClientImp {

  /**
   * The char sent to the server to set ASCII
   */
  private static final String ASCII_CHAR = "A";

  /**
   * The char sent to the server to set BINARY
   */
  private static final String BINARY_CHAR = "I";

  private static final String CMD_LIST = "LIST ";

  private static final String CMD_NLST = "NLST ";

  /**
   * Socket responsible for controlling the connection
   */
  private ControlSocket control = null;

  /**
   * Socket responsible for transferring the data
   */
  private DataSocket dataSocket = null;

  /**
   * Socket timeout for both data and control. In milliseconds
   */
  private int timeout = 0;

  /**
   * Record of the transfer type - make the default ASCII
   */
  private TransferType transferType = TransferType.ASCII;

  /**
   * Record of the connect mode - make the default PASV (as this was the
   * original mode supported)
   */
  private FtpDataMode connectMode = FtpDataMode.PASSIVE;

  /**
   * Holds the last valid reply from the server on the control socket
   */
  private Reply lastValidReply;

  private TimezoneDateHandler tzHandler = new TimezoneDateHandler(TimeZone.getDefault());

  private boolean currentlyConnected = false;

  // -rwxrwxrwx 1 user group 28725 Jun 30 09:38 RAC DMAIL FUEL 10X2 100X73.pdf
  private static final String LIST_DIR_FULL = "^(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)"
      + "\\s+(\\d*)\\s+(\\S*)\\s+(\\S*)\\s+(\\S*)\\s+(.*)";

  private static final Pattern LIST_DIR_PATTERN = Pattern.compile(LIST_DIR_FULL);

  /**
   * @see FtpClient#FtpClient(String, int, int)
   */
  public FtpClient(String remoteHost) throws IOException, FileTransferException {
    this(remoteHost, ControlSocket.CONTROL_PORT, 0);
  }

  /**
   * @see FtpClient#FtpClient(String, int, int)
   */
  public FtpClient(String remoteHost, int controlPort) throws IOException, FileTransferException {
    this(remoteHost, controlPort, 0);
  }

  /**
   * @see FtpClient#FtpClient(String, int, int)
   */
  public FtpClient(InetAddress remoteAddr) throws IOException, FileTransferException {
    this(remoteAddr, ControlSocket.CONTROL_PORT);
  }

  /**
   * @see FtpClient#FtpClient(String, int, int)
   */
  public FtpClient(InetAddress remoteAddr, int controlPort) throws IOException, FileTransferException {
    this(remoteAddr, controlPort, 0);
  }

  /**
   * Constructor. Creates the control socket
   *
   * @param remoteHost the remote hostname
   * @param controlPort port for control stream
   * @param timeout the length of the timeout, in milliseconds
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public FtpClient(String remoteHost, int controlPort, int timeout) throws IOException, FileTransferException {

    control = new ControlSocket(remoteHost, controlPort, timeout);
  }

  /**
   * @see FtpClient#FtpClient(String, int, int)
   */
  public FtpClient(InetAddress remoteAddr, int controlPort, int timeout) throws IOException, FileTransferException {
    this(remoteAddr.getHostAddress(), controlPort, timeout);
  }

  /**
   * Set the FTP Server timezone handler for modification times.
   * <p>
   * If not explicitly set, then the server is assumed to be in the same
   * timezone as the client; this could lead to incorrect modification times
   * being reported.
   * </p>
   *
   * @param tz the handler.
   */
  public void setServerTimezone(TimeZone tz) {
    tzHandler = new TimezoneDateHandler(tz);
  }

  /**
   * Set the TCP timeout on the underlying socket.
   *
   * If a timeout is set, then any operation which takes longer than the timeout
   * value will be killed with a java.io.InterruptedException. We set both the
   * control and data connections
   *
   * @param millis The length of the timeout, in milliseconds
   * @throws IOException if a comms error occurs
   */
  public void setTimeout(int millis) throws IOException {

    timeout = millis;
    control.setTimeout(millis);
  }

  /**
   * Set the connect mode PASV is the default, and shouldn't need to change.
   *
   * @param mode ACTIVE or PASV mode
   */
  public void setDataMode(FtpDataMode mode) {
    connectMode = mode;
  }

  /**
   * @see com.adaptris.filetransfer.FileTransferClient#connect(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void connect(String user, String password) throws IOException, FileTransferException {

    String response = control.sendCommand("USER " + user);
    lastValidReply = control.validateReply(response, "331");
    response = control.sendCommand("PASS " + password);
    lastValidReply = control.validateReply(response, "230");
    currentlyConnected = true;
  }

  /**
   * Supply the user name to log into an account on the FTP server. Must be
   * followed by the password() method - but we allow for
   *
   * @param user user name
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public void user(String user) throws IOException, FileTransferException {

    String reply = control.sendCommand("USER " + user);

    lastValidReply = control.validateReply(reply, new String[]
    {
        "230", "331"
    });
  }

  /**
   * Supplies the password for a previously supplied username to log into the
   * FTP server. Must be preceeded by the user() method
   *
   * @param password user's password
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public void password(String password) throws IOException, FileTransferException {

    String reply = control.sendCommand("PASS " + password);

    // we allow for a site with no passwords (202)
    lastValidReply = control.validateReply(reply, new String[]
    {
        "230", "202"
    });
    currentlyConnected = true;
  }

  /**
   * Get the name of the remote host
   *
   * @return remote host name
   */
  String getRemoteHostName() {
    return control.getRemoteHostName();
  }

  /**
   * Issue arbitrary ftp commands to the FTP server.
   *
   * @param command ftp command to be sent to server
   * @param validCodes valid return codes for this command
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public void quote(String command, String[] validCodes) throws IOException, FileTransferException {

    String reply = control.sendCommand(command);

    // allow for no validation to be supplied
    if (validCodes != null && validCodes.length > 0) {
      lastValidReply = control.validateReply(reply, validCodes);
    }
  }

  /**
   * @see com.adaptris.filetransfer.FileTransferClient#put(java.io.InputStream,
   *      java.lang.String, boolean)
   */
  @Override
  public void put(InputStream srcStream, String remoteFile, boolean append) throws IOException, FileTransferException {

    // get according to set type
    if (getType() == TransferType.ASCII) {
      putASCII(srcStream, remoteFile, append);
    }
    else {
      putBinary(srcStream, remoteFile, append);
    }
    validateTransfer();
  }

  /**
   * Validate that the put() or get() was successful
   *
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  private void validateTransfer() throws IOException, FileTransferException {
    String reply = control.readReply();
    lastValidReply = control.validateReply(reply, new String[]
    {
        "226", "250"
    });
  }

  /**
   * Request the server to set up the put
   *
   * @param remoteFile name of remote file in current directory
   * @param append true if appending, false otherwise
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  private void initPut(String remoteFile, boolean append) throws IOException, FileTransferException {

    // set up data channel
    dataSocket = control.createDataSocket(connectMode);
    dataSocket.setTimeout(timeout);

    // send the command to store
    String cmd = append ? "APPE " : "STOR ";
    String reply = control.sendCommand(cmd + remoteFile);

    // Can get a 125 or a 150
    lastValidReply = control.validateReply(reply, new String[]
    {
        "125", "150"
    });
  }

  /**
   * Put as ASCII, i.e. read a line at a time and write inserting the correct
   * FTP separator
   *
   * @param srcStream input stream of data to put
   * @param remoteFile name of remote file we are writing to
   * @param append true if appending, false otherwise
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  private void putASCII(InputStream srcStream, String remoteFile, boolean append) throws IOException, FileTransferException {

    // need to read line by line ...
    LineNumberReader in = new LineNumberReader(new InputStreamReader(srcStream));

    initPut(remoteFile, append);

    // get an character output stream to write to ... AFTER we
    // have the ok to go ahead AND AFTER we've successfully opened a
    // stream for the local file
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()));

    // write line by line, writing \r\n as required by RFC959 after
    // each line
    String line = null;
    while ((line = in.readLine()) != null) {
      out.write(line, 0, line.length());
      out.write(ControlSocket.EOL, 0, ControlSocket.EOL.length());
    }
    in.close();
    out.flush();
    out.close();

    // and close the data socket
    try {
      dataSocket.close();
    }
    catch (IOException ignore) {
      ;
    }
  }

  /**
   * Put as binary, i.e. read and write raw bytes
   *
   * @param srcStream input stream of data to put
   * @param remoteFile name of remote file we are writing to
   * @param append true if appending, false otherwise
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  private void putBinary(InputStream srcStream, String remoteFile, boolean append) throws IOException, FileTransferException {

    BufferedInputStream in = new BufferedInputStream(srcStream);

    initPut(remoteFile, append);

    // get an output stream
    BufferedOutputStream out = new BufferedOutputStream(new DataOutputStream(dataSocket.getOutputStream()));

    byte[] buf = new byte[512];

    // read a chunk at a time and write to the data socket
    long size = 0;
    int count = 0;
    while ((count = in.read(buf)) > 0) {
      out.write(buf, 0, count);
      size += count;
    }

    in.close();

    // flush and clean up
    out.flush();
    out.close();

    // and close the data socket
    try {
      dataSocket.close();
    }
    catch (IOException ignore) {
      ;
    }

    // log bytes transferred
    control.log("Transferred " + size + " bytes to remote host");
  }

  /**
   * @see com.adaptris.filetransfer.FileTransferClient#get(java.io.OutputStream,
   *      java.lang.String)
   */
  @Override
  public void get(OutputStream destStream, String remoteFile) throws IOException, FileTransferException {

    // get according to set type
    if (getType() == TransferType.ASCII) {
      getASCII(destStream, remoteFile);
    }
    else {
      getBinary(destStream, remoteFile);
    }
    validateTransfer();
  }

  /**
   * Request to the server that the get is set up
   *
   * @param remoteFile name of remote file
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  private void initGet(String remoteFile) throws IOException, FileTransferException {

    // set up data channel
    dataSocket = control.createDataSocket(connectMode);
    dataSocket.setTimeout(timeout);

    // send the retrieve command
    String reply = control.sendCommand("RETR " + remoteFile);
    lastValidReply = control.validateReply(reply, new String[]
    {
        "125", "150"
    });
  }

  /**
   * Get as ASCII, i.e. read a line at a time and write using the correct
   * newline separator for the OS
   *
   * @param destStream data stream to write data to
   * @param remoteFile name of remote file
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  private void getASCII(OutputStream destStream, String remoteFile) throws IOException, FileTransferException {

    initGet(remoteFile);

    // create the buffered stream for writing
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(destStream));

    // get an character input stream to read data from ... AFTER we
    // have the ok to go ahead
    LineNumberReader in = new LineNumberReader(new InputStreamReader(dataSocket.getInputStream()));

    // B. McKeown:
    // If we are in active mode we have to set the timeout of the passive
    // socket. We can achieve this by calling setTimeout() again.
    // If we are in passive mode then we are merely setting the value twice
    // which does no harm anyway. Doing this simplifies any logic changes.
    dataSocket.setTimeout(timeout);

    // read/write a line at a time
    IOException storedEx = null;
    String line = null;
    try {
      while ((line = in.readLine()) != null) {
        out.write(line, 0, line.length());
        out.newLine();
      }
    }
    catch (IOException ex) {
      storedEx = ex;
    }
    finally {
      out.flush();
    }

    try {
      in.close();
      dataSocket.close();
    }
    catch (IOException ignore) {
      ;
    }

    // if we failed to write the file, rethrow the exception
    if (storedEx != null) {
      throw storedEx;
    }
  }

  /**
   * Get as binary file, i.e. straight transfer of data
   *
   * @param destStream stream to write to
   * @param remoteFile name of remote file
   */
  private void getBinary(OutputStream destStream, String remoteFile) throws IOException, FileTransferException {

    initGet(remoteFile);

    // create the buffered output stream for writing the file
    BufferedOutputStream out = new BufferedOutputStream(destStream);

    // get an input stream to read data from ... AFTER we have
    // the ok to go ahead AND AFTER we've successfully opened a
    // stream for the local file
    BufferedInputStream in = new BufferedInputStream(new DataInputStream(dataSocket.getInputStream()));

    // B. McKeown:
    // If we are in active mode we have to set the timeout of the passive
    // socket. We can achieve this by calling setTimeout() again.
    // If we are in passive mode then we are merely setting the value twice
    // which does no harm anyway. Doing this simplifies any logic changes.
    dataSocket.setTimeout(timeout);

    // do the retrieving
    long size = 0;
    int chunksize = 4096;
    byte[] chunk = new byte[chunksize];
    int count;
    IOException storedEx = null;

    // read from socket & write to file in chunks
    try {
      while ((count = in.read(chunk, 0, chunksize)) >= 0) {
        out.write(chunk, 0, count);
        size += count;
      }
    }
    catch (IOException ex) {
      storedEx = ex;
    }
    finally {
      out.flush();
    }

    // close streams
    try {
      in.close();
      dataSocket.close();
    }
    catch (IOException ignore) {
      ;
    }

    // if we failed to write to the stream, rethrow the exception
    if (storedEx != null) {
      throw storedEx;
    }

    // log bytes transferred
    control.log("Transferred " + size + " bytes from remote host");
  }

  /**
   * @see com.adaptris.filetransfer.FileTransferClient#get(java.lang.String)
   */
  @Override
  public byte[] get(String remoteFile) throws IOException, FileTransferException {
    ByteArrayOutputStream destStream = new ByteArrayOutputStream(4096);
    // get according to set type
    if (getType() == TransferType.ASCII) {
      getASCII(destStream, remoteFile);
    }
    else {
      getBinary(destStream, remoteFile);
    }
    destStream.close();
    validateTransfer();
    return destStream.toByteArray();
  }

  /**
   * Run a site-specific command on the server. Support for commands is
   * dependent on the server
   *
   * @param command the site command to run
   * @return true if command ok, false if command not implemented
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public boolean site(String command) throws IOException, FileTransferException {

    // send the retrieve command
    String reply = control.sendCommand("SITE " + command);

    // Can get a 200 (ok) or 202 (not impl). Some
    // FTP servers return 502 (not impl)
    lastValidReply = control.validateReply(reply, new String[]
    {
        "200", "202", "502"
    });

    // return true or false? 200 is ok, 202/502 not
    // implemented
    if (reply.substring(0, 3).equals("200")) {
      return true;
    }
    return false;
  }

  private String[] ls(String cmd, String dirname) throws IOException, FileTransferException {
    // set up data channel
    dataSocket = control.createDataSocket(connectMode);
    dataSocket.setTimeout(timeout);
    String command = cmd;
    if (dirname != null) {
      command += dirname;
    }
    // some FTP servers bomb out if NLST has whitespace appended
    command = command.trim();
    String reply = control.sendCommand(command);
    // check the control response. wu-ftp returns 550 if the
    // directory is empty, so we handle 550 appropriately. Similarly
    // proFTPD returns 450
    lastValidReply = control.validateReply(reply, new String[]
    {
        "125", "150", "450", "550"
    });
    // an empty array of files for 450/550
    String[] result = new String[0];
    // a normal reply ... extract the file list
    String replyCode = lastValidReply.getReplyCode();
    if (!replyCode.equals("450") && !replyCode.equals("550")) {
      LineNumberReader in = new LineNumberReader(new InputStreamReader(dataSocket.getInputStream()));
      Vector lines = new Vector();
      String line = null;
      while ((line = in.readLine()) != null) {
        control.log("Processing [" + line + "]");
        String filename = line;
        if (line.substring(line.lastIndexOf("/") + 1).equals(".") || line.substring(line.lastIndexOf("/") + 1).equals("..")) {
          control.log("Ignoring special files . and ..");
          continue;
        }
        lines.add(filename);
      }
      try {
        in.close();
        dataSocket.close();
      }
      catch (IOException ignore) {
        ;
      }
      reply = control.readReply();
      lastValidReply = control.validateReply(reply, new String[]
      {
          "226", "250"
      });
      if (!lines.isEmpty()) {
        result = (String[]) lines.toArray(result);
      }
    }
    return result;
  }

  private String[] shortDir(String dirname) throws IOException, FileTransferException {
    String[] files = ls(CMD_NLST, dirname);
    Vector results = new Vector();
    for (String file : files) {
      String filename = file;
      // This is a hack
      // on vsftpd we get
      // -rw-r--r-- 1 502 100 35 Jul 08 12:14 file-1.txt
      // on wu-ftpd we get
      // -rw-r--r-- 1 502 100 35 Jul 08 12:14 /junit/vio/file-1.txt
      // even though the "dirname" passed in is /junit/vio/file-1.txt
      if (file.indexOf("/") > -1) {
        File f = new File(file);
        filename = f.getName();
      }
      results.add(filename);
    }
    return (String[]) results.toArray(new String[0]);
  }

  private String[] longDir(String dirname) throws IOException, FileTransferException {

    String[] files = ls(CMD_LIST, dirname);
    Vector results = new Vector();
    for (String file : files) {
      String filename = file;
      Matcher m = LIST_DIR_PATTERN.matcher(file);
      if (!m.matches()) {
        logR.trace("Ignoring [" + file + "]");
        continue;
      }
      results.add(filename);
    }
    return (String[]) results.toArray(new String[0]);
  }

  /**
   * @see FileTransferClient#dir(java.lang.String, boolean)
   */
  @Override
  public String[] dir(String dirname, boolean full) throws IOException, FileTransferException {
    return full ? longDir(dirname) : shortDir(dirname);
  }

  /**
   * Gets the latest valid reply from the server
   *
   * @return reply object encapsulating last valid server response
   */
  public Reply getLastValidReply() {
    return lastValidReply;
  }

  /**
   * Switch debug of responses on or off
   *
   * @param on true if you wish to have responses to the log stream, false
   *          otherwise
   */
  public void debugResponses(boolean on) {
    control.debugResponses(on);
  }

  /**
   * Get the current transfer type
   *
   * @return the current type of the transfer, i.e. BINARY or ASCII
   */
  public TransferType getType() {
    return transferType;
  }

  /**
   * Set the transfer type
   *
   * @param type the transfer type to set the server to
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public void setType(TransferType type) throws IOException, FileTransferException {

    String typeStr = BINARY_CHAR;
    // determine the character to send
    if (type == TransferType.ASCII) {
      typeStr = ASCII_CHAR;
    }
    // send the command
    String reply = control.sendCommand("TYPE " + typeStr);
    lastValidReply = control.validateReply(reply, "200");

    // record the type
    transferType = type;
  }

  /**
   * @see FileTransferClient#delete(java.lang.String)
   */
  @Override
  public void delete(String remoteFile) throws IOException, FileTransferException {

    String reply = control.sendCommand("DELE " + remoteFile);
    lastValidReply = control.validateReply(reply, "250");
  }

  /**
   * @see FileTransferClient#rename(java.lang.String, java.lang.String)
   */
  @Override
  public void rename(String from, String to) throws IOException, FileTransferException {

    String reply = control.sendCommand("RNFR " + from);
    lastValidReply = control.validateReply(reply, "350");

    reply = control.sendCommand("RNTO " + to);
    lastValidReply = control.validateReply(reply, "250");
  }

  /**
   * @see FileTransferClient#rmdir(java.lang.String)
   */
  @Override
  public void rmdir(String dir) throws IOException, FileTransferException {

    String reply = control.sendCommand("RMD " + dir);

    // some servers return 257, technically incorrect but
    // we cater for it ...
    lastValidReply = control.validateReply(reply, new String[]
    {
        "250", "257"
    });
  }

  /**
   * @see FileTransferClient#mkdir(java.lang.String)
   */
  @Override
  public void mkdir(String dir) throws IOException, FileTransferException {

    String reply = control.sendCommand("MKD " + dir);
    lastValidReply = control.validateReply(reply, "257");
  }

  /**
   * @see FileTransferClient#chdir(java.lang.String)
   */
  @Override
  public void chdir(String dir) throws IOException, FileTransferException {

    String reply = control.sendCommand("CWD " + dir);
    lastValidReply = control.validateReply(reply, "250");
  }

  /**
   * Get modification time for a remote file
   *
   * @param remoteFile name of remote file
   * @return modification time of file as a date
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   * @deprecated use {@link #lastModifiedDate(String)} instead for consistency
   */
  @Deprecated
  public Date modtime(String remoteFile) throws IOException, FileTransferException {
    return lastModifiedDate(remoteFile);
  }

  /**
   *
   * @see FileTransferClient#lastModifiedDate(java.lang.String)
   */
  @Override
  public Date lastModifiedDate(String remoteFile) throws IOException, FileTransferException {

    String reply = control.sendCommand("MDTM " + remoteFile);
    lastValidReply = control.validateReply(reply, "213");

    // parse the reply string ...
    return tzHandler.asDate(lastValidReply.getReplyText());
  }

  /**
   *
   * @see FileTransferClient#lastModified(java.lang.String)
   */
  @Override
  public long lastModified(String remoteFile) throws IOException, FileTransferException {
    String reply = control.sendCommand("MDTM " + remoteFile);
    lastValidReply = control.validateReply(reply, "213");

    // parse the reply string ...
    return tzHandler.asLong(lastValidReply.getReplyText());
  }

  /**
   * Get the current remote working directory
   *
   * @return the current working directory
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public String pwd() throws IOException, FileTransferException {

    String reply = control.sendCommand("PWD");
    lastValidReply = control.validateReply(reply, "257");

    // get the reply text and extract the dir
    // listed in quotes, if we can find it. Otherwise
    // just return the whole reply string
    String text = lastValidReply.getReplyText();
    int start = text.indexOf('"');
    int end = text.lastIndexOf('"');
    if (start >= 0 && end > start) {
      return text.substring(start + 1, end);
    }
    else {
      return text;
    }
  }

  /**
   * Get the type of the OS at the server
   *
   * @return the type of server OS
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public String system() throws IOException, FileTransferException {

    String reply = control.sendCommand("SYST");
    lastValidReply = control.validateReply(reply, "215");
    return lastValidReply.getReplyText();
  }

  /**
   * Get the help text for the specified command
   *
   * @param command name of the command to get help on
   * @return help text from the server for the supplied command
   * @throws FileTransferException if an FTP specific exception occurs
   * @throws IOException if a comms error occurs
   */
  public String help(String command) throws IOException, FileTransferException {

    String reply = control.sendCommand("HELP " + command);
    lastValidReply = control.validateReply(reply, new String[]
    {
        "211", "214"
    });
    return lastValidReply.getReplyText();
  }

  /**
   *
   * @see com.adaptris.filetransfer.FileTransferClient#disconnect()
   */
  @Override
  public void disconnect() throws IOException, FileTransferException {

    try {
      if (currentlyConnected) {
        String reply = control.sendCommand("QUIT");
        lastValidReply = control.validateReply(reply, new String[]
        {
            "221", "226"
        });
      }
    }
    finally { // ensure we clean up the connection
      control.logout();
      control = null;
    }
  }

  private class TimezoneDateHandler {

    /**
     * Format to interpret MTDM timestamp
     */
    private transient SimpleDateFormat tsFormat;

    TimezoneDateHandler(TimeZone tz) {
      tsFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      if (tz != null) {
        tsFormat.setTimeZone(tz);
      }
    }

    Date asDate(String mdtmString) {
      return tsFormat.parse(mdtmString, new ParsePosition(0));
    }

    long asLong(String mdtmString) {
      return asDate(mdtmString).getTime();
    }
  }

  @Override
  public long getKeepAliveTimeout() throws FtpException {
    throw new FtpException("Keep Alive Timeout not supported on this FTP client");
  }

  @Override
  public void setKeepAliveTimeout(long seconds) throws FtpException{
    throw new FtpException("Keep Alive Timeout not supported on this FTP client");
  }

  //Currently not properly handled so it'll just create a new connection each time
  @Override
  public boolean isConnected(){
    return false;
  }

}
