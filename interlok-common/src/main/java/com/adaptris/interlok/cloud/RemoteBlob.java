package com.adaptris.interlok.cloud;

/**
 * 
 * Abstraction of a blob that is stored in the cloud (e.g. an Azure blob object, AWS S3 blob etc).
 * 
 */
public class RemoteBlob {

  private String name;
  private long lastModified;
  private long size;
  private String bucket;

  private RemoteBlob() {

  }

  /**
   * The name of the remote blob.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  private RemoteBlob withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get the last modified time of the blob.
   * 
   * @return the last modified time, or -1 if not known/unavailable
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Wrap it as a {@link RemoteFile} for standard {@link FileFilter} operations.
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

  /**
   * Get the size of the blob if available.
   * 
   * @return the size of the blob, or -1 if not available / unknown.
   */
  public long getSize() {
    return size;
  }

  private RemoteBlob withSize(long size) {
    this.size = size;
    return this;
  }

  /**
   * Get the bucket that this blob resides in.
   * 
   * @return the bucket name, might be null.
   * 
   */
  public String getBucket() {
    return bucket;
  }

  private RemoteBlob withBucket(String bucket) {
    this.bucket = bucket;
    return this;
  }


  public static class Builder {
    private transient String name;
    private transient long lastModified = -1;
    private transient long size = -1;
    private transient String bucket;

    public RemoteBlob build() {
      return new RemoteBlob().withName(name).withLastModified(lastModified).withSize(size).withBucket(bucket);
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
  }
}
