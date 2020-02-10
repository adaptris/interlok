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
import static org.junit.Assert.assertEquals;
import org.junit.Assume;
import org.junit.Test;
import com.adaptris.filetransfer.FileTransferClient;
import com.adaptris.ftp.FtpDataMode;
import com.adaptris.ftp.TransferType;
import com.adaptris.security.password.Password;

public class FtpConnectionTest extends FtpPasswordConnectionCase {

  public static final String CFG_HOST = "FtpConsumerTest.host";
  public static final String CFG_USER = "FtpConsumerTest.username";
  public static final String CFG_PASSWORD = "FtpConsumerTest.password";
  public static final String CFG_REMOTE_DIR = "FtpConsumerTest.remotedir";

  @Override
  protected FtpConnectionImp createConnection() {
    FtpConnectionImp c = createConnectionObj();
    c.setDefaultPassword(PROPERTIES.getProperty(CFG_PASSWORD));
    c.setDefaultUserName(PROPERTIES.getProperty(CFG_USER));
    c.setTransferType(TransferType.BINARY);
    c.setFtpDataMode(FtpDataMode.PASSIVE);
    c.setAdditionalDebug(true);
    return c;
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  protected FtpConnectionImp createConnectionObj() {
    return new FtpConnection();
  }

  @Test
  public void testConnect_WithAccount() throws Exception {
    Assume.assumeTrue(areTestsEnabled());
    FtpConnectionImp connection = createConnection();
    connection.setDefaultAccount(getName());
    try {
      start(connection);
      FileTransferClient client = connection.connect(getDestinationString());
    } finally {
      stop(connection);
    }
  }

  @Override
  protected String getDestinationString() {
    return getScheme() + "://" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverrideUserOnly() throws Exception {
    return getScheme() + "://" + PROPERTIES.getProperty(CFG_USER)
        + "@" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverride() throws Exception {
    return getScheme() + "://" + PROPERTIES.getProperty(CFG_USER) + ":" + Password.decode(PROPERTIES.getProperty(CFG_PASSWORD))
        + "@" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  protected String getScheme() {
    return "ftp";
  }

  @Override
  protected void assertDefaultControlPort(int defaultControlPort) {
    assertEquals(FtpConnectionImp.DEFAULT_FTP_CONTROL_PORT, defaultControlPort);
  }

}
