package com.adaptris.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of {@link FsWorker} that appeneds to the file for any write operations.
 * 
 * @config fs-append-file
 */
@XStreamAlias("fs-append-file")
public class AppendingFsWorker extends StandardWorker {

  @Override
  public void put(byte[] data, File file) throws FsException {
    OutputStream out = null;

    try {
      out = new FileOutputStream(file, true);
      out.write(data);
    }
    catch (IOException e) {
      throw new FsException(e);
    }
    finally {
      closeQuietly(out);
    }
  }
}