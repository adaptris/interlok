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

package com.adaptris.core.services.metadata.compare;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * <p>
 * Compares two dates using {@link Date#compareTo(Date)}. The result will be the result of that operation as a string so effectively
 * {@code -1, 0, or 1}.
 * </p>
 * 
 * @config metadata-compare-timestamps
 * @author lchan
 * 
 */
@XStreamAlias("metadata-compare-timestamps")
@AdapterComponent
@ComponentProfile(summary = "Compares a configured metadata timestamp value against teh supplied value.", tag = "operator,comparator,metadata")
public class CompareTimestamps extends ComparatorImpl {

  private static final String DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

  @InputFieldDefault(value = DEFAULT_FORMAT)
  private String dateFormat;

  public CompareTimestamps() {
    super();
  }

  public CompareTimestamps(String result) {
    this();
    setResultKey(result);
  }

  public CompareTimestamps(String result, String format) {
    this(result);
    setDateFormat(format);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) throws ServiceException {
    MetadataElement result = new MetadataElement();
    result.setKey(getResultKey());
    try {
      result.setValue(String.valueOf(compareFormattedDates(firstItem.getValue(), secondItem.getValue())));
    } catch (ParseException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return result;
  }

  String dateFormat() {
    return !isEmpty(getDateFormat()) ? getDateFormat() : DEFAULT_FORMAT;
  }
  /**
   * @return the dateFormat
   */
  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * Set the date format to parse the metadata values.
   * 
   * @param f the dateFormat to set, the default is {@code yyyy-MM-dd'T'HH:mm:ssZ} if not specified.
   */
  public void setDateFormat(String f) {
    this.dateFormat = f;
  }

  @Override
  protected boolean compare(String a, String b) {
    try {
      return compareFormattedDates(a, b) == 0;
    } catch (ParseException e) {
      return false;
    }
  }

  private int compareFormattedDates(String a, String b) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat());
    Date firstDate = sdf.parse(a);
    Date secondDate = sdf.parse(b);
    return firstDate.compareTo(secondDate);
  }
}
