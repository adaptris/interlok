package com.adaptris.fs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link FsWorker} that uses overwrites the file if it already exists.
 * 
 * @config fs-overwrite-file
 */
@XStreamAlias("fs-overwrite-file")
public class OverwriteIfExistsWorker extends NioWorker {

  @Override
  public void put(byte[] data, File file) throws FsException {
    if (file.exists() && !file.delete()) {
      throw new FsException("Could not delete [" + file + "]");
    }
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(file, "rw");
      raf.write(data);
    }
    catch (IOException e) {
      throw new FsException(e);
    }
    finally {
      closeQuietly(raf);
    }
  }
}