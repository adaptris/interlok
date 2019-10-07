package com.adaptris.filetransfer;

import java.io.File;

// Used to make FileFilters work with remote FTP files.
// It's probably quite dangerous, but since most of our filters don't
// really do much in the way of low-level file access it's lastModified/size/name etc
// we should be OK.
public class RemoteFile extends File {

  private long length = -1;
  private long lastModified = -1;
  private boolean isDirectory;
  private boolean isFile;

  public RemoteFile(String filepath) {
    super(filepath);
  }

  @Override
  public long length() {
    return length;
  }

  @Override
  public boolean setLastModified(long time) {
    lastModified = time;
    return true;
  }

  @Override
  public long lastModified() {
    return lastModified;
  }

  @Override
  public boolean isFile() {
    return isFile;
  }

  @Override
  public boolean isDirectory() {
    return isDirectory;
  }

  public RemoteFile withLength(long size) {
    this.length = size;
    return this;
  }

  public RemoteFile withLastModified(long time) {
    setLastModified(time);
    return this;
  }

  public RemoteFile withIsDirectory(boolean b) {
    isDirectory = b;
    return this;
  }

  public RemoteFile withIsFile(boolean b) {
    isFile = b;
    return this;
  }
}
