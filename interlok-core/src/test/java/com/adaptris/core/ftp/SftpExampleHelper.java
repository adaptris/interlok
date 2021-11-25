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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.GaussianIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.RandomIntervalPoller;
import com.adaptris.sftp.ConfigBuilder;
import com.adaptris.sftp.HostConfig;
import com.adaptris.sftp.OpenSSHConfigBuilder;
import com.adaptris.sftp.PerHostConfigBuilder;
import com.adaptris.sftp.SftpClient;
import com.adaptris.util.KeyValuePair;

public class SftpExampleHelper {
  public static final String CFG_HOST = "SftpConsumerTest.host";
  public static final String CFG_PORT = "SftpConsumerTest.port";
  public static final String CFG_USER = "SftpConsumerTest.username";
  public static final String CFG_PASSWORD = "SftpConsumerTest.password";
  public static final String CFG_REMOTE_DIR = "SftpConsumerTest.remotedir";

  // DO NOT use the top two directly, use @setupTempHostsFile and the CFG_TEMP_HOSTS_FILE
  public static final String CFG_KNOWN_HOSTS_FILE = "SftpConsumerTest.knownHostsFile";
  public static final String CFG_UNKNOWN_HOSTS_FILE = "SftpConsumerTest.unknownHostsFile";
  public static final String CFG_TEMP_HOSTS_FILE = "SftpConsumerTest.tempHostsFile";

  public static final String CFG_PRIVATE_KEY_FILE = "SftpConsumerTest.privateKeyFile";
  public static final String CFG_PRIVATE_KEY_PW = "SftpConsumerTest.privateKeyPassword";

  private enum ConnectionType {
    Standard() {

      @Override
      FileTransferConnection create() {
        return standardSftpConnection();
      }

    };

    abstract FileTransferConnection create();

  };

  private enum ConfigType {
    OpenSSH() {

      @Override
      ConfigBuilder create() {
        return createOpensshRepo();
      }

    },
    PerHost() {
      @Override
      ConfigBuilder create() {
        return createPerHostConfigRepo();
      }
    },
    Inline() {
      @Override
      ConfigBuilder create() {
        return createInlineConfigRepo();
      }
    };

    abstract ConfigBuilder create();

  };

  public static List<FileTransferConnection> createConnectionsForExamples() {
    List<FileTransferConnection> result = new ArrayList<>();
    for (ConnectionType connectionType : ConnectionType.values()) {
      for (ConfigType confType : ConfigType.values()) {
        FileTransferConnection con = connectionType.create();
        ConfigBuilder builder = confType.create();
        setConfigBuilder(con, builder);
        result.add(con);
      }
    }
    return result;
  }

  public static Poller[] createPollers() {
    return new Poller[] {
        new QuartzCronPoller("*/20 * * * * ?"), new FixedIntervalPoller(), new RandomIntervalPoller(), new GaussianIntervalPoller()
    };
  }
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

  public static ConfigBuilder createOpensshRepo() {
    return new OpenSSHConfigBuilder("/path/openssh/config/file");
  }

  public static ConfigBuilder createInlineConfigRepo() {
    return new InlineConfigRepositoryBuilder(false).build();
  }

  public static File createOpenSshConfig(File targetDir, boolean strict) throws Exception {
    targetDir.mkdirs();
    File tempFile = File.createTempFile(SftpExampleHelper.class.getSimpleName(), "", targetDir);
    try (PrintStream out = new PrintStream(new FileOutputStream(tempFile))) {
      out.println("Host *");
      out.println("  StrictHostKeyChecking " + BooleanUtils.toStringYesNo(strict));
      out.println("  " + SftpClient.SSH_PREFERRED_AUTHENTICATIONS + " " + SftpClient.NO_KERBEROS_AUTH);
    }
    return tempFile;
  }

  public static PerHostConfigBuilder createPerHostConfigRepo() {
    PerHostConfigBuilder inline = new PerHostConfigBuilder();
    HostConfig a = new HostConfig("my.host.com", null, -1, new KeyValuePair("StrictHostKeyChecking", "yes"),
        new KeyValuePair(SftpClient.SSH_PREFERRED_AUTHENTICATIONS, SftpClient.NO_KERBEROS_AUTH));
    HostConfig b = new HostConfig("another.host.com", null, -1, new KeyValuePair("StrictHostKeyChecking", "no"),
        new KeyValuePair(SftpClient.SSH_PREFERRED_AUTHENTICATIONS, SftpClient.NO_KERBEROS_AUTH));
    inline.getHosts().add(a);
    inline.getHosts().add(b);
    return inline;
  }

  public static void setConfigBuilder(Object obj, ConfigBuilder builder) {
    try {
      Method m = getMethod(obj.getClass(), "setConfiguration");
      m.invoke(obj, new Object[]
      {
          builder
      });
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String getConfigSimpleName(Object obj) {
    try {
      Method m = getMethod(obj.getClass(), "getConfiguration");
      Object o = m.invoke(obj, (Object[]) null);
      return o.getClass().getSimpleName();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Method getMethod(Class c, String methodName) {
    Method result = null;
    Method[] methods = c.getMethods();
    for (Method m : methods) {
      String name = m.getName();
      if (name.equalsIgnoreCase(methodName)) {
        result = m;
        break;
      }
    }
    return result;
  }

}
