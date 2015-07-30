package com.adaptris.fs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link FsWorker} that uses java.nio to perform put and get operations.
 * 
 * @config fs-nio-worker
 */
@XStreamAlias("fs-nio-worker")
public class NioWorker extends StandardWorker {

  @Override
  public void put(byte[] data, File file) throws FsException {
    if (file.exists()) {
      throw new FsException("trying to write to file [" + file + "] which exists");
    }
    write(data, file);
  }

  protected void write(byte[] data, File file) throws FsException {
    ByteBuffer buffer = ByteBuffer.wrap(data);
    RandomAccessFile raf = null;
    FileChannel channel = null;
    FileLock lock = null;

    try {
      raf = new RandomAccessFile(file, "rw");
      channel = raf.getChannel();
      lock = channel.lock();
      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }
    }
    catch (IOException e) {
      throw new FsException(e);
    }
    finally {
      releaseQuietly(lock);
      closeQuietly(channel);
      closeQuietly(raf);
    }
  }

  @Override
  public byte[] get(File file) throws FsException {
    RandomAccessFile raf = null;
    FileChannel channel = null;
    FileLock lock = null;
    ByteBuffer buffer = null;

    try {
      raf = new RandomAccessFile(checkAcl(file), "rw"); // rw for lock
      channel = raf.getChannel();
      lock = channel.lock();
      buffer = ByteBuffer.allocate((int) raf.length());
      while (buffer.hasRemaining()) {
        channel.read(buffer);
      }
    }
    catch (IOException e) {
      throw new FsException(e);
    }
    finally {
      releaseQuietly(lock);
      closeQuietly(channel);
      closeQuietly(raf);
    }
    return buffer.array();
  }

  private void releaseQuietly(FileLock lock) {
    try {
      if (lock != null) {
        lock.release();
      }
    }
    catch (Exception e) {

    }

  }
}