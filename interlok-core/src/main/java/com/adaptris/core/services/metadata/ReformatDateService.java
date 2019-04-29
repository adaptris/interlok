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

import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Reformats the date and time stored against a metadata key.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be treated as a date to be reformatted.
 * </p>
 * <p>
 * In addition to supporting all the patterns allowed by {@link java.text.SimpleDateFormat}, this service also supports the special
 * values
 * {@code SECONDS_SINCE_EPOCH} and {@code MILLISECONDS_SINCE_EPOCH} which describe the number of seconds and milliseconds since
 * midnight Jan 1, 1970 UTC respectively. If specified as the source format, then the {@code long} value will be converted into a
 * {@link java.util.Date} before formatting (scientific notation is supported as per {@link
 * java.math.BigDecimal#BigDecimal(String)}); if specified as the destination format, then the raw long value will be emitted.
 * </p>
 * 
 * @config reformat-date-service
 * 
 * 
 * @see java.text.SimpleDateFormat
 * @see com.adaptris.util.text.DateFormatUtil.CustomDateFormat
 * 
 */
@XStreamAlias("reformat-date-service")
@AdapterComponent
@ComponentProfile(summary = "Reformat a data value stored in metadata", tag = "service,metadata,timestamp,datetime")
@DisplayOrder(order = {"metadataKeyRegexp", "sourceDateFormat", "sourceFormatBuilder", "destinationDateFormat",
    "destinationFormatBuilder", "metadataLogger"})
public class ReformatDateService extends ReformatMetadata {

  private static final DateFormatBuilder DEFAULT_FORMAT_BUILDER = new DateFormatBuilder();

  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use sourceFormatBuilder")
  private String sourceDateFormat;
  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use destinationFormatBuilder")
  private String destinationDateFormat;

  @Valid
  private DateFormatBuilder sourceFormatBuilder;
  @Valid
  private DateFormatBuilder destinationFormatBuilder;

  public ReformatDateService() {
    super();
  }

  public ReformatDateService(String regexp) {
    super(regexp);
  }

  public ReformatDateService(String regexp, DateFormatBuilder srcFormat, DateFormatBuilder destFormat) {
    this(regexp);
    setSourceFormatBuilder(srcFormat);
    setDestinationFormatBuilder(destFormat);
  }

  @Override
  public String reformat(String s, AdaptrisMessage msg) throws Exception {
    return destFormatBuilder().build(msg).toString(sourceFormatBuilder().build(msg).toDate(s));
  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
    if (!isBlank(getSourceDateFormat())) {
      log.warn("source-date-format is deprecated, use source-format-builder instead");
    }
    if (!isBlank(getDestinationDateFormat())) {
      log.warn("destination-date-format is deprecated, use destination-format-builder instead");
    }
  }

  /**
   * @return the sourceDateFormat
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use sourceFormatBuilder")
  public String getSourceDateFormat() {
    return sourceDateFormat;
  }

  /**
   * The format with which to parse the source date into a Date object
   * 
   * @see java.text.SimpleDateFormat
   * @param s the sourceDateFormat to set
   * @deprecated since 3.6.6 use {@link #setSourceFormatBuilder(DateFormatBuilder)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use sourceFormatBuilder")
  public void setSourceDateFormat(String s) {
    this.sourceDateFormat = s;
  }

  /**
   * @return the destinationDateFormat
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use destinationFormatBuilder")
  public String getDestinationDateFormat() {
    return destinationDateFormat;
  }

  /**
   * The format in which to output to the destination key
   * 
   * @see java.text.SimpleDateFormat
   * @param s the destinationDateFormat to set
   * @deprecated since 3.6.6 use {@link #setDestinationFormatBuilder(DateFormatBuilder)} instead.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use destinationFormatBuilder")
  public void setDestinationDateFormat(String s) {
    this.destinationDateFormat = s;
  }

  public DateFormatBuilder getSourceFormatBuilder() {
    return sourceFormatBuilder;
  }

  public void setSourceFormatBuilder(DateFormatBuilder sourceFormatBuilder) {
    this.sourceFormatBuilder = sourceFormatBuilder;
  }

  DateFormatBuilder sourceFormatBuilder() {
    DateFormatBuilder result = getSourceFormatBuilder();
    if (!isBlank(getSourceDateFormat())) {
      log.warn("source-date-format is deprecated, use source-format-builder instead");
      result = new DateFormatBuilder(getSourceDateFormat());
    }
    return result != null ? result : DEFAULT_FORMAT_BUILDER;
  }

  public DateFormatBuilder getDestinationFormatBuilder() {
    return destinationFormatBuilder;
  }

  public void setDestinationFormatBuilder(DateFormatBuilder destinationFormatBuilder) {
    this.destinationFormatBuilder = destinationFormatBuilder;
  }

  DateFormatBuilder destFormatBuilder() {
    DateFormatBuilder result = getDestinationFormatBuilder();
    if (!isBlank(getDestinationDateFormat())) {
      log.warn("destination-date-format is deprecated, use destination-format-builder instead");
      result = new DateFormatBuilder(getDestinationDateFormat());
    }
    return result != null ? result : DEFAULT_FORMAT_BUILDER;
  }

}
