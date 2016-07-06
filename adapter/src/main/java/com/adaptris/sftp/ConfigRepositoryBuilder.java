package com.adaptris.sftp;

import java.io.IOException;

import com.jcraft.jsch.ConfigRepository;

/**
 * Interface for managing SSH configuration specifics.
 * 
 * @author lchan
 *
 */
public interface ConfigRepositoryBuilder {

  /**
   * Build a {@link ConfigRepository}.
   * 
   * @return a {@link ConfigRepository}
   */
  ConfigRepository build() throws IOException;

}
