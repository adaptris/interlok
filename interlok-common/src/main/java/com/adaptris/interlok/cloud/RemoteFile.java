package com.adaptris.interlok.cloud;

import java.io.File;

// Used to make FileFilters work with remote FTP files.
// It's probably quite dangerous, but since most of our filters don't
// really do much in the way of low-level file access it's lastModified/size/name etc
// we should be OK.
public class RemoteFile extends File {

  private static final long serialVersionUID = 2019100801L;

  private long length = -1;
  private long lastModified = -1;
  private boolean isDirectory;
  private boolean isFile;

  private RemoteFile(String filepath) {
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


  private RemoteFile withLength(long size) {
    this.length = size;
    return this;
  }

  private RemoteFile withLastModified(long time) {
    setLastModified(time);
    return this;
  }

  private RemoteFile withIsDirectory(boolean b) {
    isDirectory = b;
    return this;
  }

  private RemoteFile withIsFile(boolean b) {
    isFile = b;
    return this;
  }

  public static class Builder {
    private long length = -1;
    private long lastModified = -1;
    private boolean isDirectory;
    private boolean isFile;
    private String path;

    public Builder() {

    }

    public RemoteFile build() {
      return
          new RemoteFile(path).withIsDirectory(isDirectory).withIsFile(isFile).withLastModified(lastModified).withLength(length);
    }

    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    public Builder setLength(long size) {
      this.length = size;
      return this;
    }

    public Builder setLastModified(long time) {
      this.lastModified = time;
      return this;
    }

    public Builder setIsDirectory(boolean b) {
      isDirectory = b;
      return this;
    }

    public Builder setIsFile(boolean b) {
      isFile = b;
      return this;
    }
  }
}
