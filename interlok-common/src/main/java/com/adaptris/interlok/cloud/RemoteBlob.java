package com.adaptris.interlok.cloud;

import lombok.Getter;

/**
 * 
 * Abstraction of a blob that is stored in the cloud (e.g. an Azure blob object, AWS S3 blob etc).
 * 
 */
@Getter
public class RemoteBlob {

  private String name;
  private long lastModified;
  private long size;
  private String bucket;
  // nullable, optional short error message
  private String errorSummary;

  private RemoteBlob withName(String name) {
    this.name = name;
    return this;
  }

    /**
   * Wrap it as a {@link RemoteFile} for standard {@link java.io.FileFilter} operations.
   * 
   */
  public RemoteFile toFile() {
    return new RemoteFile.Builder().setPath(getName()).setIsDirectory(false).setIsFile(true)
        .setLastModified(getLastModified()).setLength(getSize()).build();
  }

  private RemoteBlob withLastModified(long lastModified) {
    this.lastModified = lastModified;
    return this;
  }

    private RemoteBlob withSize(long size) {
    this.size = size;
    return this;
  }

    private RemoteBlob withBucket(String bucket) {
    this.bucket = bucket;
    return this;
  }

  private RemoteBlob withErrorSummary(String errorSummary) {
    this.errorSummary = errorSummary;
    return this;
  }


  public static class Builder {
    private transient String name;
    private transient long lastModified = -1;
    private transient long size = -1;
    private transient String bucket;
    private transient String errorSummary; // nullable

    public RemoteBlob build() {
      return new RemoteBlob().withName(name).withLastModified(lastModified).withSize(size).withBucket(bucket)
          .withErrorSummary(errorSummary);
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setLastModified(long lastModified) {
      this.lastModified = lastModified;
      return this;
    }

    public Builder setSize(long size) {
      this.size = size;
      return this;
    }

    public Builder setBucket(String bucket) {
      this.bucket = bucket;
      return this;
    }

    public Builder setErrorSummary(String errorSummary) {
      this.errorSummary = errorSummary;
      return this;
    }
  }
}
