/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Standard implementation of <code>FileNameCreator</code> which constructs a file name from an optional configurable prefix, the
 * message's unique ID and an optional configurable suffix.
 * </p>
 * 
 * @deprecated since 2.8.2, use {@link FormattedFilenameCreator} instead
 * @config standard-file-name-creator
 */
@Deprecated
@XStreamAlias("standard-file-name-creator")
public class StandardFileNameCreator implements FileNameCreator {
  private static transient boolean warningLogged = false;
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @AdvancedConfig
  private String prefix;
  @AdvancedConfig
  private String suffix;
  @AdvancedConfig
  private boolean useMessageUniqueId;
  @AdvancedConfig
  private boolean addTimeStamp;

  /**
   * <p>
   * Creates a new instance. Default prefix and suffix are "", therefore without
   * additional configuration, the filename will be the message's unique ID.
   * </p>
   */
  public StandardFileNameCreator() {
    if (!warningLogged) {
      log.warn("StandardFilenameCreator is deprecated; use {} instead", FormattedFilenameCreator.class.getCanonicalName());
      warningLogged = true;
    }
    // defaults...
    setPrefix("");
    setSuffix("");
    setUseMessageUniqueId(true);
    setAddTimeStamp(false);
  }

  /**
   * <p>
   * Creates file name from the configured prefix plus (if useMessageUniqueId is
   * true) the message's unique ID, plus the configured suffix.  If the
   * resulting name's length is 0, a new <code>CoreException</code> is thrown.
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to create a file name for
   * @return the file name
   * @throws CoreException if the resulting file name's length is 0
   * @see com.adaptris.core.FileNameCreator#createName
   *   (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public String createName(AdaptrisMessage msg) throws CoreException {
    StringBuffer result = new StringBuffer();

    result.append(prefix);

    if (getUseMessageUniqueId()) {
      result.append(msg.getUniqueId());
    }

    if (getAddTimeStamp()) {
      result.append("." + System.currentTimeMillis());
    }

    result.append(suffix);

    if (result.length() == 0) {
      throw new CoreException
        ("StandardFileNameCreator has created a 0 length file name");
    }

    return result.toString();
  }

  /**
   * <p>
   * Returns the <code>String</code> prefix used to create filenames.
   * </p>
   * @return the <code>String</code> prefix used to create filenames
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * <p>
   * Returns the <code>String</code> suffix used to create filenames.
   * </p>
   * @return the <code>String</code> suffix used to create filenames
   */
  public String getSuffix() {
    return suffix;
  }

  /**
   * <p>
   * Returns true if the message's unique ID is used to create the file name.
   * </p>
   * @return true if the message's unique ID is used to create the file name
   */
  public boolean getUseMessageUniqueId() {
    return useMessageUniqueId;
  }

  /**
   * <p>
   * Sets the <code>String</code> prefix to use to create the file name.  May be
   * empty but not null.
   * </p>
   * @param string the <code>String</code> prefix to use to create the file name
   */
  public void setPrefix(String string) {
    if (string == null) {
      throw new IllegalArgumentException("prefix may not be null");
    }
    prefix = string;
  }

  /**
   * <p>
   * Sets the <code>String</code> suffix to use to create the file name.  May be
   * empty but not null.
   * </p>
   * @param string the <code>String</code> suffix to use to create the file name
   */
  public void setSuffix(String string) {
    if (string == null) {
      throw new IllegalArgumentException("suffix may not be null");
    }
    suffix = string;
  }

  /**
   * <p>
   * Sets whether or not to use the message's unique ID as part of the file
   * name.  If this is set to false there is an increased chance of file name
   * collisions.
   * </p>
   * @param b whether or not to use the message's unique ID as part of the file
   * name
   */
  public void setUseMessageUniqueId(boolean b) {
    if (!b) {
      log.warn(
        "StandardFileName creator is not using message unique ID, "
          + "file name collisions possible");
    }

    useMessageUniqueId = b;
  }

  /** Add a timestamp to the file as an extension.
   *
   * @param b true to add the timestamp.
   */
  public void setAddTimeStamp(boolean b) {
    addTimeStamp = b;
  }

  /** Get the addTimestamp flag.
   *
   * @return the flag.
   */
  public boolean getAddTimeStamp() {
    return addTimeStamp;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();

    result.append("[");
    result.append(this.getClass().getName());
    result.append("] prefix [");
    result.append(prefix);
    result.append("] suffix [");
    result.append(suffix);
    result.append("] use message ID [");
    result.append(useMessageUniqueId);
    result.append("]");

    return result.toString();
  }
}
