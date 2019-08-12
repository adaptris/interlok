package com.adaptris.core.ftp;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.sftp.ConfigBuilder;
import com.adaptris.sftp.InlineConfigBuilder;
import com.adaptris.sftp.SftpClient;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;


public class InlineConfigRepositoryBuilder {
  private boolean strictHostChecking = false;

  public InlineConfigRepositoryBuilder(boolean strict) {
    strictHostChecking = strict;
  }


  public ConfigBuilder build() {
    InlineConfigBuilder repo = new InlineConfigBuilder();
    repo.getConfig().addAll(config());
    return repo;
  }

  protected KeyValuePairSet config() {
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("compression.s2c", "zlib,none"));
    kvps.add(new KeyValuePair("compression.c2s", "zlib,none"));
    kvps.add(new KeyValuePair("StrictHostKeyChecking", BooleanUtils.toStringYesNo(strictHostChecking)));
    kvps.add(new KeyValuePair(SftpClient.SSH_PREFERRED_AUTHENTICATIONS, SftpClient.NO_KERBEROS_AUTH));
    return kvps;
  }
}
