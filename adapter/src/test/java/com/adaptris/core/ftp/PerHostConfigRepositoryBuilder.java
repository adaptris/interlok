package com.adaptris.core.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.adaptris.sftp.ConfigBuilder;
import com.adaptris.sftp.HostConfig;
import com.adaptris.sftp.PerHostConfigBuilder;


public class PerHostConfigRepositoryBuilder extends InlineConfigRepositoryBuilder {
  private List<String> knownHosts = new ArrayList<>();

  public PerHostConfigRepositoryBuilder(File known_hosts, boolean strict) throws IOException {
    super(strict);
    try (InputStream in = new FileInputStream(known_hosts)) {
      List<String> lines = IOUtils.readLines(in);
      for (String line : lines) {
        String[] parts=line.split("\\s");
        knownHosts.add(parts[0]);
        System.err.println("[" + parts[0] + "]");
      }
    }
  }


  @Override
  public ConfigBuilder build() {
    PerHostConfigBuilder repo = new PerHostConfigBuilder();
    repo.getDefaultConfiguration().addAll(config());
    for (String host : knownHosts) {
      HostConfig cfg = new HostConfig();
      cfg.setHostname(host);
      cfg.getConfig().addAll(config());
      repo.getHosts().add(cfg);
    }
    return repo;
  }
}
