package com.adaptris.interlok.util;

import java.io.File;
import java.io.FileFilter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AcceptAny implements FileFilter {

  @Override
  public boolean accept(File pathname) {
    return true;
  }

}