package com.adaptris.core.services.metadata;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds a DateFormat instance for use with {@link ReformatDateService} and {@link AddTimestampMetadataService}.
 *
 * <p>Setting a format of: yyyy-MM-dd'T'HH:mm:ss would successfully parse (supports up to 6 decimals):
 * <ul>
 *   <li>2018-08-01T00:00:00.000000Z</li>
 *   <li>2018-08-01T00:00:00.000000</li>
 *   <li>2018-08-01T00:00:00Z</li>
 *   <li>2018-08-01T00:00:00</li>
 * </ul>
 * </p>
 *
 * @config date-format-builder-with-optional-fraction-and-offset
 * @author mwarman
 */
@XStreamAlias("date-format-builder-with-optional-fraction-and-offset")
@DisplayOrder(order = {"format", "locale", "timezone"})
public class DateFormatBuilderWithOptionalFractionAndOffset extends DateFormatBuilder {

  public DateFormatBuilder.DateFormatter build(AdaptrisMessage msg) {
    return withTimeZone(createWithLocale(msg), msg.resolve(getTimezone()));
  }

  private DateTimeFormatter createWithLocale(AdaptrisMessage msg) {
    String language = msg.resolve(getLanguageTag());
    String format = msg.resolve(getFormat());
    DateTimeFormatterBuilder dateFormatterBuilder = new DateTimeFormatterBuilder()
        .appendPattern(format)
        .appendFraction(ChronoField.NANO_OF_SECOND , 0 , 6 , true)
        .optionalStart()
        .appendOffsetId()
        .optionalEnd();
    return (!isBlank(language)) ? dateFormatterBuilder.toFormatter(Locale.forLanguageTag(language)) : dateFormatterBuilder.toFormatter();
  }

  private DateFormatBuilder.DateFormatter withTimeZone(DateTimeFormatter format, String id) {
    ZoneId zoneId = (!isBlank(id)) ? ZoneId.of(id) : ZoneId.systemDefault();
    format.withZone(zoneId);
    return new XmlDateFormatter(format, zoneId);
  }

  public class XmlDateFormatter implements DateFormatBuilder.DateFormatter {

    private DateTimeFormatter formatter;
    private ZoneId zoneId;

    public XmlDateFormatter(DateTimeFormatter formatter, ZoneId zoneId){
      this.formatter = formatter;
      this.zoneId = zoneId;
    }

    @Override
    public Date toDate(String dateString) throws ParseException {
      try {
        LocalDateTime ldt = LocalDateTime.parse(dateString, formatter);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
      } catch (DateTimeParseException e){
        throw new ParseException(dateString, e.getErrorIndex());
      }
    }

    @Override
    public String toString(Date d) {
      Instant instant = d.toInstant();
      ZonedDateTime ldt = instant.atZone(zoneId);
      return ldt.format(formatter);
    }
  }

}
