package com.adaptris.core.services.metadata;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.util.text.DateFormatUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Reformats the date and time stored against a metadata key.
 * <p>
 * Each matching metadata key from {@link ReformatMetadata#getMetadataKeyRegexp()} will be treated as a date to be reformatted.
 * </p>
 * <p>
 * In addition to supporting all the patterns allowed by {@link SimpleDateFormat}, this service also supports the special values
 * {@code SECONDS_SINCE_EPOCH} and {@code MILLISECONDS_SINCE_EPOCH} which describe the number of seconds and milliseconds since
 * midnight Jan 1, 1970 UTC respectively. If specified as the source format, then the {@code long} value will be converted into a
 * {@link java.util.Date} before formatting (scientific notation is supported as per {@link BigDecimal#BigDecimal(String)}); if
 * specified as the destination format, then the raw long value will be emitted.
 * </p>
 * 
 * @config reformat-date-service
 * 
 * @license BASIC
 * @see SimpleDateFormat
 * @see com.adaptris.util.text.DateFormatUtil.CustomDateFormat
 * 
 */
@XStreamAlias("reformat-date-service")
public class ReformatDateService extends ReformatMetadata {

  static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  @NotBlank
  @AutoPopulated
  private String sourceDateFormat;
  @NotBlank
  @AutoPopulated
  private String destinationDateFormat;

  /**
   * Constructor
   * <ul>
   * <li>sourceDateFormat = yyyy-MM-dd'T'HH:mm:ssZ</li>
   * <li>destinationDateFormat = yyyy-MM-dd'T'HH:mm:ssZ</li>
   * </ul>
   */
  public ReformatDateService() {
    super();
    setSourceDateFormat(DEFAULT_DATE_FORMAT);
    setDestinationDateFormat(DEFAULT_DATE_FORMAT);
  }

  public ReformatDateService(String regexp) {
    super(regexp);
    setSourceDateFormat(DEFAULT_DATE_FORMAT);
    setDestinationDateFormat(DEFAULT_DATE_FORMAT);
  }

  @Override
  protected String reformat(String s, String msgCharset) throws Exception {
    return DateFormatUtil.toString(DateFormatUtil.toDate(s, getSourceDateFormat()), getDestinationDateFormat());
  }

  @Override
  public void init() throws CoreException {
    super.init();
  }

  @Override
  public void close() {
  }

  /**
   * @return the sourceDateFormat
   */
  public String getSourceDateFormat() {
    return sourceDateFormat;
  }

  /**
   * The format with which to parse the source date into a Date object
   * 
   * @see SimpleDateFormat
   * @param s the sourceDateFormat to set
   */
  public void setSourceDateFormat(String s) {
    this.sourceDateFormat = s;
  }

  /**
   * @return the destinationDateFormat
   */
  public String getDestinationDateFormat() {
    return destinationDateFormat;
  }

  /**
   * The format in which to output to the destination key
   * 
   * @see SimpleDateFormat
   * @param s the destinationDateFormat to set
   */
  public void setDestinationDateFormat(String s) {
    this.destinationDateFormat = s;
  }
}
