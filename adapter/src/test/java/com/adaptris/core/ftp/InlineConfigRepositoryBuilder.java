package com.adaptris.core.ftp;

import org.apache.commons.lang.BooleanUtils;

import com.adaptris.sftp.ConfigRepositoryBuilder;
import com.adaptris.sftp.InlineConfigRepository;
import com.adaptris.sftp.SftpClient;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;


public class InlineConfigRepositoryBuilder {
  private boolean strictHostChecking = false;

  public InlineConfigRepositoryBuilder(boolean strict) {
    strictHostChecking = strict;
  }


  public ConfigRepositoryBuilder build() {
    InlineConfigRepository repo = new InlineConfigRepository();
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
