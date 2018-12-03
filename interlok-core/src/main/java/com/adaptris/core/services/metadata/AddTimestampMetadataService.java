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

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.metadata.timestamp.OffsetTimestampGenerator;
import com.adaptris.core.services.metadata.timestamp.TimestampGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Adds timestamp information as metadata.
 * <p>
 * In addition to supporting all the patterns allowed by {@link java.text.SimpleDateFormat}, this service also supports the special
 * values {@code SECONDS_SINCE_EPOCH} and {@code MILLISECONDS_SINCE_EPOCH} which describe the number of seconds and milliseconds
 * since midnight Jan 1, 1970 UTC respectively. If specified as the format, then the long value associated will be emitted.
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
@ComponentProfile(summary = "Add a timestamp as metadata", tag = "service,metadata,timestamp,datetime")
@DisplayOrder(order = {"metadataKey", "dateFormat", "dateFormatBuilder", "offset", "alwaysReplace"})
public class AddTimestampMetadataService extends ServiceImp {
  private static final DateFormatBuilder DEFAULT_FORMAT_BUILDER = new DateFormatBuilder();

  private static final TimestampGenerator DEFAULT_GENERATOR = new TimestampGenerator() {

    @Override
    public Date generateTimestamp(AdaptrisMessage msg) throws ServiceException {
      return new Date();
    }
    
  };

  @NotBlank
  @AutoPopulated
  @AffectsMetadata
  private String metadataKey;
  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use DateFormatBuilder")
  private String dateFormat;
  @InputFieldDefault(value = "false")
  private Boolean alwaysReplace;
  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use TimestampGenerator")
  private String offset;

  @AdvancedConfig
  @Valid
  @InputFieldDefault(value = "null, so now()")
  private TimestampGenerator timestampGenerator = null;
  @Valid
  private DateFormatBuilder dateFormatBuilder;

  public AddTimestampMetadataService() {
    this(DateFormatBuilder.DEFAULT_DATE_FORMAT, "timestamp");
  }

  public AddTimestampMetadataService(String format, String metadataKey) {
    this(format, metadataKey, null);
  }

  public AddTimestampMetadataService(String format, String metadataKey, Boolean alwaysReplace) {
    this(format, metadataKey, alwaysReplace, "");
  }

  public AddTimestampMetadataService(String format, String metadataKey, Boolean alwaysReplace, String offset) {
    this(format, metadataKey, alwaysReplace, isBlank(offset) ? (TimestampGenerator) null : new OffsetTimestampGenerator(offset));
  }

  public AddTimestampMetadataService(String format, String metadataKey, Boolean alwaysReplace, TimestampGenerator s) {
    super();
    setDateFormatBuilder(new DateFormatBuilder(format));
    setMetadataKey(metadataKey);
    setAlwaysReplace(alwaysReplace);
    setTimestampGenerator(s);
  }

  /**
   *
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (!msg.headersContainsKey(getMetadataKey()) || alwaysReplace()) {
      msg.addMetadata(getMetadataKey(), formatBuilder().build(msg).toString(timestampGenerator().generateTimestamp(msg)));
    }
    else {
      log.trace(getMetadataKey() + " already exists, no replacement");
    }
  }


  @Override
  protected void initService() throws CoreException {
    if (isBlank(getMetadataKey())) {
      throw new CoreException("No Metadata key specified for timestamp");
    }
    warnDateFormat();
    if (!isBlank(getOffset())) {
      log.warn("Use of deprecated offset; use {} instead", OffsetTimestampGenerator.class.getSimpleName());
      setTimestampGenerator(new OffsetTimestampGenerator(getOffset()));
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
   * @deprecated since 3.6.6 use {@link #getDateFormatBuilder()} instead
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use DateFormatBuilder")
  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * @param dateFormat the dateFormat to set
   * @deprecated since 3.6.6 use {@link #setDateFormatBuilder(DateFormatBuilder)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use DateFormatBuilder")
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

  /**
   * 
   * @deprecated since 3.5.0 Use {@link OffsetTimestampGenerator} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use TimestampGenerator")
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
   * @deprecated since 3.5.0 Use {@link OffsetTimestampGenerator} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use TimestampGenerator")
  public void setOffset(String offset) {
    this.offset = offset;
  }

  @Override
  public void prepare() throws CoreException {
  }

  /**
   * @return the timestampGenerator
   */
  public TimestampGenerator getTimestampGenerator() {
    return timestampGenerator;
  }

  /**
   * @param s the timestampGenerator to set
   */
  public void setTimestampGenerator(TimestampGenerator s) {
    this.timestampGenerator = s;
  }

  TimestampGenerator timestampGenerator() {
    return getTimestampGenerator() != null ? getTimestampGenerator() : DEFAULT_GENERATOR;
  }

  public DateFormatBuilder getDateFormatBuilder() {
    return dateFormatBuilder;
  }

  public void setDateFormatBuilder(DateFormatBuilder builder) {
    this.dateFormatBuilder = builder;
  }

  DateFormatBuilder formatBuilder() {
    DateFormatBuilder result = getDateFormatBuilder();
    if (!isBlank(getDateFormat())) {
      warnDateFormat();
      result = new DateFormatBuilder(getDateFormat());
    }
    return result != null ? result : DEFAULT_FORMAT_BUILDER;
  }

  private void warnDateFormat() {
    if (!isBlank(getDateFormat())) {
      log.warn("date-format is deprecated, use date-format-builder instead");
    }
  }
}
