package com.adaptris.sftp;

import com.adaptris.core.ftp.SftpConnectionTest;

public class OpenSSHConfigBuilderTest extends ConfigBuilderCase {

  @Override
  protected OpenSSHConfigBuilder createBuilder() throws Exception {
    return new OpenSSHConfigBuilder(SftpConnectionTest.createOpenSshConfig(false).getCanonicalPath());
  }


}
