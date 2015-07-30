package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;

public class YesOrNo implements FileFilter {

  private boolean accept = true;

  public YesOrNo(String expr) {
    accept = Boolean.parseBoolean(expr);
  }


  /**
   * @see java.io.FileFilter#accept(java.io.File)
   */
  public boolean accept(File pathname) {
    return accept;
  }
}
