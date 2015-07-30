package com.adaptris.core.fs;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * An entry in the {@link ProcessedItemCache} that marks a file that was already processed when using a
 * {@link NonDeletingFsConsumer}.
 * 
 * @config processed-item
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("processed-item")
public class ProcessedItem implements Serializable {

  private String absolutePath;
  private long lastModified;
  private long filesize;
  private long lastProcessed;

  /**
   *
   */
  private static final long serialVersionUID = 2006070101L;

  public ProcessedItem() {
    setLastProcessed(System.currentTimeMillis());
  }

  public ProcessedItem(String name, long lastModTime, long size) {
    this();
    setAbsolutePath(name);
    setLastModified(lastModTime);
    setFilesize(size);
    setLastProcessed(System.currentTimeMillis());
  }

  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified(long l) {
    lastModified = l;
  }

  public void setAbsolutePath(String s) {
    if (s == null) {
      throw new IllegalArgumentException("Absolute path = null");
    }
    absolutePath = s.replaceAll("//", "/");

  }

  public String getAbsolutePath() {
    return absolutePath;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }

  @Override
  public boolean equals(Object other) {
    boolean result = false;
    if (other instanceof ProcessedItem && null != other) {
      result = absolutePath.equals(((ProcessedItem) other).getAbsolutePath());
    }
    return result;
  }

  @Override
  public int hashCode() {
    return absolutePath.hashCode();
  }

  /**
   * @return the lastProcessed
   */
  public long getLastProcessed() {
    return lastProcessed;
  }

  /**
   * @param l the lastProcessed to set
   */
  public void setLastProcessed(long l) {
    lastProcessed = l;
  }

}
