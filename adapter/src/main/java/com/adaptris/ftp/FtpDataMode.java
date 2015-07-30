package com.adaptris.ftp;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Represents the DATA mode.
 * 
 */
public enum FtpDataMode {
  ACTIVE {
    @Override
    public void applyDataMode(FTPClient ftp) {
      ftp.enterLocalActiveMode();
    }
  },
  PASSIVE {
    @Override
    public void applyDataMode(FTPClient ftp) {
      ftp.enterLocalPassiveMode();
    }
  };

  public abstract void applyDataMode(FTPClient client);
}
