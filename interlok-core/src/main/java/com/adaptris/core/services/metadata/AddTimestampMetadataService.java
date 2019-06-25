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
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.metadata.timestamp.OffsetTimestampGenerator;
import com.adaptris.core.services.metadata.timestamp.TimestampGenerator;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
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

  private static final TimestampGenerator DEFAULT_GENERATOR = (msg) -> {
    return new Date();
  };

  @NotBlank(message = "Must provide a metadata key")
  @AutoPopulated
  @AffectsMetadata
  private String metadataKey;
  @InputFieldDefault(value = "false")
  private Boolean alwaysReplace;

  @AdvancedConfig
  @Valid
  @InputFieldDefault(value = "null, so now()")
  private TimestampGenerator timestampGenerator = null;
  @Valid
  @NotNull(message = "Must provide a date format")
  @AutoPopulated
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
  @Override
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
    try {
      Args.notBlank(getMetadataKey(), "metadata-key");
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
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
    return BooleanUtils.toBooleanDefaultIfNull(getAlwaysReplace(), false);
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
    return ObjectUtils.defaultIfNull(getTimestampGenerator(), DEFAULT_GENERATOR);
  }

  public DateFormatBuilder getDateFormatBuilder() {
    return dateFormatBuilder;
  }

  public void setDateFormatBuilder(DateFormatBuilder builder) {
    this.dateFormatBuilder = builder;
  }

  DateFormatBuilder formatBuilder() {
    return ObjectUtils.defaultIfNull(getDateFormatBuilder(), DEFAULT_FORMAT_BUILDER);
  }

}
