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

package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.text.DateFormatUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Adds timestamp information as metadata.
 * <p>
 * In addition to supporting all the patterns allowed by {@link  java.text.SimpleDateFormat}, this service also supports the special values
 * {@code SECONDS_SINCE_EPOCH} and {@code MILLISECONDS_SINCE_EPOCH} which describe the number of seconds and milliseconds since
 * midnight Jan 1, 1970 UTC respectively. If specified as the format, then the long value associated will be emitted.
 * </p>
 * 
 * @config add-timestamp-metadata-service
 * @see com.adaptris.util.text.DateFormatUtil.CustomDateFormat
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("add-timestamp-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Add a timestamp as metadata", tag = "service,metadata")
@DisplayOrder(order = {"metadataKey", "dateFormat", "offset", "alwaysReplace"})
public class AddTimestampMetadataService extends ServiceImp {

  @NotBlank
  @AutoPopulated
  private String metadataKey;
  @NotBlank
  @AutoPopulated
  private String dateFormat;
  @InputFieldDefault(value = "false")
  private Boolean alwaysReplace;
  @AdvancedConfig
  private String offset;

  /**
   * Default constructor.
   * <ul>
   * <li>DateFormat is "yyyy-MM-dd'T'HH:mm:ssZ"</li>
   * <li>MetadataKey is "timestamp"</li>
   * <li>AlwaysReplace is false</li>
   * </ul>
   */
  public AddTimestampMetadataService() {
    this("yyyy-MM-dd'T'HH:mm:ssZ", "timestamp");
  }

  /**
   * Convenience constructor
   *
   * @param format the date format
   * @param metadataKey the metadata key
   */
  public AddTimestampMetadataService(String format, String metadataKey) {
    this(format, metadataKey, null);
  }

  /**
   * Convenience constructor
   *
   * @param format the date format.
   * @param metadataKey the metadataKey
   * @param alwaysReplace whether to always replace the metadata key.
   */
  public AddTimestampMetadataService(String format, String metadataKey, Boolean alwaysReplace) {
    this(format, metadataKey, alwaysReplace, null);
  }

  /**
   * Convenience constructor
   *
   * @param format the date format.
   * @param metadataKey the metadataKey
   * @param alwaysReplace whether to always replace the metadata key.
   * @param offset the offset from "now"
   */
  public AddTimestampMetadataService(String format, String metadataKey, Boolean alwaysReplace, String offset) {
    super();
    setDateFormat(format);
    setMetadataKey(metadataKey);
    setAlwaysReplace(alwaysReplace);
    setOffset(offset);
  }

  /**
   *
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (!msg.containsKey(getMetadataKey()) || alwaysReplace()) {
      msg.addMetadata(getMetadataKey(), DateFormatUtil.toString(createTimestamp(), getDateFormat()));
    }
    else {
      log.trace(getMetadataKey() + " already exists, no replacement");
    }
  }

  private Date createTimestamp() throws ServiceException {
    Date timestamp = new Date();
    try {
      if (!isBlank(offset)) {
        Duration duration;
        duration = DatatypeFactory.newInstance().newDuration(offset);
        duration.addTo(timestamp);
      }
    }
    catch (Exception e) {
      throw new ServiceException("Failed to parse " + offset + " using ISO8601", e);
    }
    return timestamp;
  }

  @Override
  protected void initService() throws CoreException {
    if (isBlank(getMetadataKey())) {
      throw new CoreException("No Metadata key specified for timestamp");
    }
    if (isBlank(getDateFormat())) {
      throw new CoreException("No date format specified");
    }
  }

  @Override
  protected void closeService() {

  }


  /**
   * @return the metadataKey
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * @param metadataKey the metadataKey to set
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

  /**
   * @return the dateFormat
   */
  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * @param dateFormat the dateFormat to set
   */
  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  /**
   * @return the alwaysReplace
   */
  public Boolean getAlwaysReplace() {
    return alwaysReplace;
  }

  /**
   * Specify whether to always replace the metadata.
   *
   * @param b the alwaysReplace to set, default is false.
   */
  public void setAlwaysReplace(Boolean b) {
    alwaysReplace = b;
  }

  boolean alwaysReplace() {
    return getAlwaysReplace() != null ? getAlwaysReplace().booleanValue() : false;
  }

  public String getOffset() {
    return offset;
  }

  /**
   * Set the offset for the timestamp.
   * <p>
   * The offset follows the ISO8601 convention for durations. The format is
   * <strong>[+-]P[n]Y[n]M[n]DT[n]H[n]M[n]S</strong>. In these representations,
   * the [n] is replaced by the value for each of the date and time elements
   * that follow the [n]. Leading zeros are not required. The capital letters
   * 'P', 'Y', 'M', 'W', 'D', 'T', 'H', 'M', and 'S' are designators for each of
   * the date and time elements and are not replaced.
   * </p>
   * <p>
   * <ul>
   * <li>P is the duration designator (historically called "period") placed at
   * the start of the duration representation.</li>
   * <li>Y is the year designator that follows the value for the number of
   * years.</li>
   * <li>M is the month designator that follows the value for the number of
   * months.</li>
   * <li>W is the week designator that follows the value for the number of
   * weeks.</li>
   * <li>D is the day designator that follows the value for the number of days.</li>
   * <li>T is the time designator that precedes the time components of the
   * representation.</li>
   * <li>H is the hour designator that follows the value for the number of
   * hours.</li>
   * <li>M is the minute designator that follows the value for the number of
   * minutes.</li>
   * <li>S is the second designator that follows the value for the number of
   * seconds.
   * </p>
   * <p>
   * For example, <code>P3Y6M4DT12H30M5S</code> represents a duration of three
   * years, six months, four days, twelve hours, thirty minutes, and five
   * seconds . Date and time elements including their designator may be omitted
   * if their value is zero, and lower order elements may also be omitted for
   * reduced precision. For example, "P23DT23H" and "P4Y" are both acceptable
   * duration representations.
   * </p>
   * <p>
   * To resolve ambiguity, "P1M" is a one-month duration and "PT1M" is a
   * one-minute duration (note the time designator, T, that precedes the time
   * value). The seconds value used may also have a decimal fraction, as in
   * "PT0.5S" to indicate half a second."PT36H" could be used as well as
   * "P1DT12H" for representing the same duration.
   * </p>
   * <p>
   * A negative period will become some date in the past; a positive or
   * unspecified period will resolve to some time in the future. For example
   * <code>-P30D</code> will resolve to 30 days ago, whereas <code>P30D</code>
   * will resolve to 30 days in the future.
   * </p>
   *
   * @param offset the offset.
   */
  public void setOffset(String offset) {
    this.offset = offset;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
