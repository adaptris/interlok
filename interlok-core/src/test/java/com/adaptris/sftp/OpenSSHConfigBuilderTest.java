package com.adaptris.sftp;

import java.io.File;

import com.adaptris.core.ftp.SftpExampleHelper;
import com.adaptris.core.stubs.TempFileUtils;

public class OpenSSHConfigBuilderTest extends ConfigBuilderCase {

  @Override
  protected OpenSSHConfigBuilder createBuilder() throws Exception {
    File targetDir = TempFileUtils.createTrackedDir(this);
    return new OpenSSHConfigBuilder(SftpExampleHelper
        .createOpenSshConfig(targetDir, false).getCanonicalPath());
  }


}
