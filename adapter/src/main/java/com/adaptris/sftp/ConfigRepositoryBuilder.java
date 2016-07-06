package com.adaptris.sftp;

import java.io.IOException;

import com.jcraft.jsch.ConfigRepository;

public interface ConfigRepositoryBuilder {

  ConfigRepository build() throws IOException;

}
