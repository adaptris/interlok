/*
 * Copyright 2017 Adaptris Ltd.
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.adaptris.util.text.DateFormatUtil.CustomDateFormat;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds a DateFormat instance for use with {@link ReformatDateService} and {@link AddTimestampMetadataService}.
 * 
 * @config date-format-builder
 */
@XStreamAlias("date-format-builder")
@DisplayOrder(order = {"format", "locale", "timezone"})
public class DateFormatBuilder {

  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

  @InputFieldHint(expression = true)
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = DEFAULT_DATE_FORMAT)
  private String format;
  @AdvancedConfig
  @InputFieldHint(expression = true, style = "java.util.Locale")
  private String languageTag;
  @InputFieldHint(expression = true, style = "java.util.TimeZone#getAvailableIDs")
  @AdvancedConfig
  private String timezone;

  private enum CustomFormatParser {
    SECONDS_SINCE_EPOCH() {

      @Override
      DateFormatter create() {
        return new SecondsSinceEpoch();
      }
    },
    MILLISECONDS_SINCE_EPOCH() {
      @Override
      DateFormatter create() {
        return new MillisecondsSinceEpoch();
      }
    };

    abstract DateFormatter create();

  }

  public DateFormatBuilder() {
    setFormat(DEFAULT_DATE_FORMAT);
  }

  public DateFormatBuilder(String format) {
    this();
    setFormat(format);
  }

  public DateFormatter build(AdaptrisMessage msg) {
    try {
      return CustomFormatParser.valueOf(CustomDateFormat.valueOf(msg.resolve(getFormat())).name()).create();
    }
    catch (IllegalArgumentException e) {
      return withTimeZone(createWithLocale(msg), msg.resolve(getTimezone()));
    }
  }

  private SimpleDateFormat createWithLocale(AdaptrisMessage msg) {
    String language = msg.resolve(getLanguageTag());
    String format = msg.resolve(getFormat());
    return (!isBlank(language)) ? new SimpleDateFormat(format, Locale.forLanguageTag(language)) : new SimpleDateFormat(format);
  }

  private DateFormatter withTimeZone(SimpleDateFormat format, String id) {
    format.setTimeZone((!isBlank(id)) ? TimeZone.getTimeZone(id) : TimeZone.getDefault());
    return new SimpleDateFormatter(format);
  }

  public String getFormat() {
    return format;
  }

  /**
   * Set the format.
   * 
   * @param format the dateformat, default is {@value #DEFAULT_DATE_FORMAT} if not specified.
   */
  public void setFormat(String format) {
    this.format = Args.notBlank(format, "format");
  }

  public DateFormatBuilder withFormat(String f) {
    setFormat(f);
    return this;
  }

  public String getLanguageTag() {
    return languageTag;
  }

  /**
   * Set the language tag for the {@link java.util.Locale} which is resolved via {@link Locale#forLanguageTag(String)}.
   * 
   * @param locale the locale using the IETF BCP 47 language tag string e.g. {@code fr-FR} or {@code en-GB}.
   * @see Locale#forLanguageTag(String)
   */
  public void setLanguageTag(String locale) {
    this.languageTag = locale;
  }

  public DateFormatBuilder withLanguageTag(String tag) {
    setLanguageTag(tag);
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  /**
   * Set the timezone
   * 
   * @param tz the timezone e.g. {@code UTC} or {@code GMT}.
   * @see java.util.TimeZone#getTimeZone(String)
   */
  public void setTimezone(String tz) {
    this.timezone = tz;
  }

  public DateFormatBuilder withTimezone(String tz) {
    setTimezone(tz);
    return this;
  }

  public interface DateFormatter {

    Date toDate(String dateString) throws ParseException;

    String toString(Date d);
  }

  private static class SimpleDateFormatter implements DateFormatter {

    private SimpleDateFormat formatter;

    private SimpleDateFormatter(SimpleDateFormat f) {
      formatter = f;
    }
    @Override
    public Date toDate(String dateString) throws ParseException {
      return formatter.parse(dateString);
    }

    @Override
    public String toString(Date d) {
      return formatter.format(d);
    }

  }

  private static class SecondsSinceEpoch implements DateFormatter {

    private SecondsSinceEpoch() {}

    public Date toDate(String stringRep) {
      return new Date(new BigDecimal(stringRep).longValue() * 1000);
    }

    public String toString(Date date) {
      return String.valueOf(new BigDecimal(date.getTime()).divide(new BigDecimal(1000), RoundingMode.HALF_UP).longValue());
    }

  }

  private static class MillisecondsSinceEpoch implements DateFormatter {

    private MillisecondsSinceEpoch() {
    }

    public Date toDate(String stringRep) {
      return new Date(new BigDecimal(stringRep).longValue());
    }

    public String toString(Date date) {
      return String.valueOf(date.getTime());
    }

  }
}
