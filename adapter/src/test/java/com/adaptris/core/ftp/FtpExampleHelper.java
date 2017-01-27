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

public class FtpExampleHelper {

  public static StandardSftpConnection standardSftpConnection() {
    StandardSftpConnection con = new StandardSftpConnection();
    con.setDefaultUserName("username");
    SftpAuthenticationWrapper auth = new SftpAuthenticationWrapper(
        new SftpPasswordAuthentication("default password if not overriden in destination"),
        new SftpKeyAuthentication("/path/to/private/key/in/openssh/format", "my_super_secret_password"),
        new SftpKeyAuthentication("/another/path/to/private/key/in/openssh/format", "another_password"));

    con.setAuthentication(auth);
    con.setKnownHostsFile("/optional/path/to/known_hosts");
    return con;
  }

  public static SftpKeyAuthConnection sftpKeyAuthConnection() {
    SftpKeyAuthConnection con = new SftpKeyAuthConnection();
    con.setDefaultUserName("username");
    con.setPrivateKeyFilename("/path/to/private/key/in/openssh/format");
    con.setPrivateKeyPassword("my_super_secret_password");
    con.setSocketTimeout(10000);
    con.setKnownHostsFile("/optional/path/to/known_hosts");
    return con;
  }

  public static SftpConnection sftpConnection() {
    SftpConnection con = new SftpConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");
    con.setKnownHostsFile("/optional/path/to/known_hosts");
    return con;
  }

  public static FtpSslConnection ftpSslConnection() {
    FtpSslConnection con = new FtpSslConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");

    return con;
  }

  public static FtpConnection ftpConnection() {
    FtpConnection con = new FtpConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");

    return con;
  }

}
