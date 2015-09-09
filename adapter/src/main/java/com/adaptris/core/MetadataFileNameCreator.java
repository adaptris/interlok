package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>FileNameCreator</code> which obtains the file name
 * to use from message metadata.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>metadata-file-name-creator</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@XStreamAlias("metadata-file-name-creator")
public class MetadataFileNameCreator implements FileNameCreator {

  private String metadataKey;
  private String defaultName;

  /**
   * <p>
   * Creates a new instance.  Default name is "MetadataFileNameCreator_default".
   * </p>
   */
  public MetadataFileNameCreator() {
    this.setDefaultName("MetadataFileNameCreator_default");
  }

  public MetadataFileNameCreator(String metadataKey) {
    this();
    setMetadataKey(metadataKey);
  }

  public MetadataFileNameCreator(String metadataKey, String defaultName) {
    this();
    setMetadataKey(metadataKey);
    setDefaultName(defaultName);
  }

  /**
   * <p>
   * Obtains the value for the configured metadata key, if this is not null or
   * empty it is returned.  If the metadata value is null or empty then the
   * configured 'default name' is returned.
   * </p>
   * @see com.adaptris.core.FileNameCreator
   *   #createName(com.adaptris.core.AdaptrisMessage)
   */
  public String createName(AdaptrisMessage msg) throws CoreException {
    if (metadataKey == null) {
      throw new CoreException("illegal metadata key [" + metadataKey + "]");
    }
    String result = msg.getMetadataValue(metadataKey);

    if (result == null || "".equals(result)) {
      result = defaultName;
    }

    return result;
  }

  /**
   * <p>
   * Returns the metadata key to look up.
   * </p>
   * @return the metadata key to look up
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * <p>
   * Sets the metadata key to look up.  May not be null or empty.
   * </p>
   * @param s the metadata key to look up
   */
  public void setMetadataKey(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("illegal param");
    }
    metadataKey = s;
  }

  /**
   * <p>
   * Sets the 'default name' which will be used if the metadata value is null
   * or empty.
   * </p>
   * @return the 'default name' which will be used if the metadata value is null
   * or empty
   */
  public String getDefaultName() {
    return defaultName;
  }

  /**
   * <p>
   * Returns the 'default name' which will be used if the metadata value is null
   * or empty.
   * </p>
   * @param s the 'default name' which will be used if the metadata value is
   * null or empty
   */
  public void setDefaultName(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("illegal param");
    }
    defaultName = s;
  }
}
