package com.adaptris.core.ftp;

import org.apache.commons.lang.BooleanUtils;

import com.adaptris.sftp.InlineConfigRepository;
import com.adaptris.util.KeyValuePair;


public class InlineConfigRepositoryBuilder {
  private boolean strictHostChecking = false;

  public InlineConfigRepositoryBuilder(boolean strict) {
    strictHostChecking = strict;
  }


  public InlineConfigRepository build() {
    InlineConfigRepository inline = new InlineConfigRepository();
    inline.getConfig().add(new KeyValuePair("compression.s2c", "zlib,none"));
    inline.getConfig().add(new KeyValuePair("compression.c2s", "zlib,none"));
    inline.getConfig().add(new KeyValuePair("StrictHostKeyChecking", BooleanUtils.toStringYesNo(strictHostChecking)));
    inline.getConfig().add(new KeyValuePair("PreferredAuthentications", "publickey,keyboard-interactive,password"));
    return inline;
  }
}
