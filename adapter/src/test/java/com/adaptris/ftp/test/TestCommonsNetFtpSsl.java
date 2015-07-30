package com.adaptris.ftp.test;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.oro.io.GlobFilenameFilter;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FtpCase;
import com.adaptris.ftp.CommonsNetFtpSslClient;
import com.adaptris.security.password.Password;

public class TestCommonsNetFtpSsl extends FtpCase {

  private static String FTP_GET_FILENAME = "ftp.get.filename";
  private static final String FTP_PUT_FILENAME = "ftp.put.filename";
  private static final String FTP_PUT_REMOTEDIR = "ftp.put.remotedir";
  private static final String FTP_GET_FILTER = "ftp.get.filter";
  private static final String FTP_HOST = "ftp.host";
  private static final String FTP_GET_REMOTEDIR = "ftp.get.remotedir";
  private static final String FTP_PASSWORD = "ftp.password";
  private static final String FTP_USERNAME = "ftp.username";

  public TestCommonsNetFtpSsl(String testName) {
    super(testName);
  }

  @Override
  protected String getRemoteGetDirectory() throws IOException {
    return config.getProperty(FTP_GET_REMOTEDIR);
  }

  @Override
  protected String getRemotePutDirectory() throws IOException {
    return config.getProperty(FTP_PUT_REMOTEDIR);
  }

  @Override
  protected String getRemoteGetFilename() throws IOException {
    return config.getProperty(FTP_GET_FILENAME);
  }

  @Override
  protected String getRemotePutFilename() throws IOException {
    return config.getProperty(FTP_PUT_FILENAME);
  }

  @Override
  protected FilenameFilter getRemoteGetFilenameFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(FTP_GET_FILTER));
  }

  @Override
  protected String getRemoteGetFilterString() {
    return config.getProperty(FTP_GET_FILTER);
  }

  @Override
  protected FileFilter getRemoteGetFileFilter() throws IOException {
    return new GlobFilenameFilter(config.getProperty(FTP_GET_FILTER));
  }

  @Override
  protected FileTransferClient connectClientImpl() throws Exception {
    CommonsNetFtpSslClient client = new CommonsNetFtpSslClient(config.getProperty(FTP_HOST));
    client.setAdditionalDebug(true);
    client.connect(config.getProperty(FTP_USERNAME), Password.decode(config.getProperty(FTP_PASSWORD)));
    return client;
  }
}
