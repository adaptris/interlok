package com.adaptris.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Not used.
 *
 * <p>
 * In the adapter configuration file this class is aliased as <b>empty-file-name-creator</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
@XStreamAlias("empty-file-name-creator")
public class EmptyFileNameCreator implements FileNameCreator {

  /**
   * <p>
   * Returns an empty <code>String</code>.
   * </p>
   *
   * @see com.adaptris.core.FileNameCreator
   *      #createName(com.adaptris.core.AdaptrisMessage)
   */
  public String createName(AdaptrisMessage msg) {
    return "";
  }
}
