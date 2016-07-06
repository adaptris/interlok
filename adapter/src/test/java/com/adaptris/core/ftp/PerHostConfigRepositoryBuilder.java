package com.adaptris.core.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;

import com.adaptris.sftp.HostConfig;
import com.adaptris.sftp.PerHostConfigRepository;
import com.adaptris.util.KeyValuePair;


public class PerHostConfigRepositoryBuilder {
  private boolean strictHostChecking = false;
  private List<String> knownHosts = new ArrayList<>();

  public PerHostConfigRepositoryBuilder(File known_hosts, boolean strict) throws IOException {
    strictHostChecking = strict;
    try (InputStream in = new FileInputStream(known_hosts)) {
      List<String> lines = IOUtils.readLines(in);
      for (String line : lines) {
        String[] parts=line.split("\\s");
        knownHosts.add(parts[0]);
        System.err.println("[" + parts[0] + "]");
      }
    }
  }


  public PerHostConfigRepository build() {
    PerHostConfigRepository inline = new PerHostConfigRepository();
    inline.getDefaultConfiguration().add(new KeyValuePair("StrictHostKeyChecking", BooleanUtils.toStringYesNo(strictHostChecking)));
    inline.getDefaultConfiguration().add(new KeyValuePair("PreferredAuthentications", "publickey,keyboard-interactive,password"));
    for (String host : knownHosts) {
      HostConfig cfg = new HostConfig();
      cfg.setHostname(host);
      cfg.getConfig().add(new KeyValuePair("StrictHostKeyChecking", BooleanUtils.toStringYesNo(strictHostChecking)));
      cfg.getConfig().add(new KeyValuePair("PreferredAuthentications", "publickey,keyboard-interactive,password"));
      inline.getHosts().add(cfg);
    }
    return inline;
  }
}
