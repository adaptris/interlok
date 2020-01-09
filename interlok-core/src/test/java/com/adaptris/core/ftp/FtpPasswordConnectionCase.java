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

package com.adaptris.core.ftp;

import static org.junit.Assert.fail;
import java.io.IOException;
import org.junit.Test;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;

public abstract class FtpPasswordConnectionCase extends FtpConnectionCase {

  @Test
  public void testConnect_BadEncodedPassword() throws Exception {
    if (areTestsEnabled()) {
      FileTransferConnectionUsingPassword connection = (FileTransferConnectionUsingPassword) createConnection();
      connection.setDefaultPassword("PW:BHFYENGMWEYQ");
      try {
        start(connection);
        FileTransferClient client = connection.connect(getDestinationString());
        fail();
      }
      catch (IOException | PasswordException expected) {

      }
      finally {
        stop(connection);
      }
    }
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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
