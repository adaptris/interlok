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

import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
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
@DisplayOrder(order = {"metadataKeyRegexp", "sourceFormatBuilder", "destinationFormatBuilder", "metadataLogger"})
public class ReformatDateService extends ReformatMetadata {

  private static final DateFormatBuilder DEFAULT_FORMAT_BUILDER = new DateFormatBuilder();

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

  public DateFormatBuilder getSourceFormatBuilder() {
    return sourceFormatBuilder;
  }

  public void setSourceFormatBuilder(DateFormatBuilder sourceFormatBuilder) {
    this.sourceFormatBuilder = sourceFormatBuilder;
  }

  DateFormatBuilder sourceFormatBuilder() {
    return ObjectUtils.defaultIfNull(getSourceFormatBuilder(), DEFAULT_FORMAT_BUILDER);
  }

  public DateFormatBuilder getDestinationFormatBuilder() {
    return destinationFormatBuilder;
  }

  public void setDestinationFormatBuilder(DateFormatBuilder destinationFormatBuilder) {
    this.destinationFormatBuilder = destinationFormatBuilder;
  }

  DateFormatBuilder destFormatBuilder() {
    return ObjectUtils.defaultIfNull(getDestinationFormatBuilder(), DEFAULT_FORMAT_BUILDER);
  }

}
