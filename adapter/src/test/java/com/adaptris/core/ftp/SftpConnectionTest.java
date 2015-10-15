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

import com.adaptris.security.password.Password;
import com.adaptris.sftp.DefaultSftpBehaviour;
import com.adaptris.sftp.StrictKnownHosts;

public class SftpConnectionTest extends FtpPasswordConnectionCase {

  public static final String CFG_HOST = "SftpConsumerTest.host";
  public static final String CFG_USER = "SftpConsumerTest.username";
  public static final String CFG_PASSWORD = "SftpConsumerTest.password";
  public static final String CFG_REMOTE_DIR = "SftpConsumerTest.remotedir";

  public SftpConnectionTest(String name) {
    super(name);
  }

  @Override
  protected SftpConnection createConnection() {
    SftpConnection c = new SftpConnection();
    c.setDefaultPassword(PROPERTIES.getProperty(CFG_PASSWORD));
    c.setDefaultUserName(PROPERTIES.getProperty(CFG_USER));
    c.setAdditionalDebug(true);
    return c;
  }

  @Override
  protected String getDestinationString() {
    return "sftp://" + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverride() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + ":" + Password.decode(PROPERTIES.getProperty(CFG_PASSWORD)) + "@"
        + PROPERTIES.getProperty(CFG_HOST) + "/" + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  @Override
  protected String getDestinationStringWithOverrideUserOnly() throws Exception {
    return "sftp://" + PROPERTIES.getProperty(CFG_USER) + "@" + PROPERTIES.getProperty(CFG_HOST) + "/"
        + PROPERTIES.getProperty(CFG_REMOTE_DIR);
  }

  protected String getScheme() {
    return "sftp";
  }

  public void testSocketTimeout() throws Exception {
    SftpConnection conn = new SftpConnection();
    assertNull(conn.getSocketTimeout());
    assertEquals(60000, conn.socketTimeout());

    conn.setSocketTimeout(10);
    assertEquals(Integer.valueOf(10), conn.getSocketTimeout());
    assertEquals(10, conn.socketTimeout());

    conn.setSocketTimeout(null);
    assertNull(conn.getSocketTimeout());
    assertEquals(60000, conn.socketTimeout());

  }

  public void testSetSshConnectionBehaviour() throws Exception {
    SftpConnection conn = new SftpConnection();
    assertNotNull(conn.getSftpConnectionBehaviour());
    assertEquals(DefaultSftpBehaviour.class, conn.getSftpConnectionBehaviour().getClass());

    StrictKnownHosts skh = new StrictKnownHosts();

    conn.setSftpConnectionBehaviour(skh);
    assertEquals(skh, conn.getSftpConnectionBehaviour());
    try {
      conn.setSftpConnectionBehaviour(null);
      fail();
    }
    catch (IllegalArgumentException expected) {

    }
    assertEquals(skh, conn.getSftpConnectionBehaviour());
  }

  protected void assertDefaultControlPort(int defaultControlPort) {
    assertEquals(SftpConnection.DEFAULT_CONTROL_PORT, defaultControlPort);
  }
}
