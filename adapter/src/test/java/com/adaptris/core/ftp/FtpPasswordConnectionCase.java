package com.adaptris.core.ftp;

import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.filetransfer.FileTransferException;
import com.adaptris.security.password.Password;

public abstract class FtpPasswordConnectionCase extends FtpConnectionCase {

  public FtpPasswordConnectionCase(String name) {
    super(name);
  }

  public void testConnect_BadEncodedPassword() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnectionUsingPassword connection = (FileTransferConnectionUsingPassword) createConnection();
      connection.setDefaultPassword("PW:BHFYENGMWEYQ");
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationString());
        fail();
      }
      catch (FileTransferException expected) {

      }
      finally {
        stop(connection);
      }
    }
  }

  public void testConnect_EncodedPassword() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnectionUsingPassword connection = (FileTransferConnectionUsingPassword) createConnection();
      String ensureEncodedPassword = Password.encode(Password.decode(connection.getDefaultPassword()), Password.PORTABLE_PASSWORD);
      connection.setDefaultPassword(ensureEncodedPassword);
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationString());
      }
      finally {
        stop(connection);
      }
    }
  }

  public void testConnect_OverrideUserOnly() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnectionUsingPassword connection = (FileTransferConnectionUsingPassword) createConnection();
      connection.setDefaultUserName(null);
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationStringWithOverrideUserOnly());
      }
      finally {
        stop(connection);
      }
    }
  }

  public void testConnect_NoPassword() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnectionUsingPassword connection = (FileTransferConnectionUsingPassword) createConnection();
      connection.setDefaultPassword(null);
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationString());
        fail();
      }
      catch (Exception expected) {

      }
      finally {
        stop(connection);
      }
    }
  }

  public void testConnect_PasswordOverride() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnectionUsingPassword connection = (FileTransferConnectionUsingPassword) createConnection();
      connection.setDefaultPassword(null);
      try {
        start(connection);
        log.trace("testConnection_PasswordOverride = " + getDestinationStringWithOverride());
        FileTransferClient client = connection.connect(getDestinationStringWithOverride());
      }
      finally {
        stop(connection);
      }
    }
  }

  protected abstract String getDestinationStringWithOverrideUserOnly() throws Exception;

}
