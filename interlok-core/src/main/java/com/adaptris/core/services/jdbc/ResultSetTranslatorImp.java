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

package com.adaptris.core.services.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.types.ColumnTranslator;
import com.adaptris.core.util.Args;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Base implementation for converting a {@linkplain java.sql.ResultSet} into an {@linkplain com.adaptris.core.AdaptrisMessage}.
 *
 * @author lchan
 *
 */
public abstract class ResultSetTranslatorImp extends StyledResultTranslatorImp {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean displayColumnErrors = null;
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private List<ColumnTranslator> columnTranslators;

  @AdvancedConfig
  @AffectsMetadata
  private String resultCountMetadataItem;
  @AdvancedConfig
  @AffectsMetadata
  private String updateCountMetadataItem;

  @Deprecated
  @Removal(version = "3.9.0")
  private String uniqueId;

  protected ResultSetTranslatorImp() {
    setColumnNameStyle(ColumnStyle.NoStyle);
    setColumnTranslators(new ArrayList<ColumnTranslator>());
  }

  @Override
  public final void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    updateMetadataUpdateCount(target, source);
    updateMetadataQueryCount(target, translateResult(source, target));;
  }

  /**
   * Translate the result returning the number of rows translated.
   *
   */
  public abstract long translateResult(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException;


  private String getValue(JdbcResultRow rs, int column) {
    String value = null;
    try {
      Object o  = rs.getFieldValue(column);
      if (o instanceof byte[]) {
        value = new String((byte[]) o);
      }
      else {
        value = o.toString();
      }
    }
    catch (Exception e1) {
      try {
        value = (String) rs.getFieldValue(column);
      }
      catch (Exception e2) {
        logColumnErrors(column, e1);
      }
    }
    return value;
  }

  protected String toString(JdbcResultRow rs, int column) {
    String result = null;
    try {
      if (getColumnTranslators().size() > 0 && column < getColumnTranslators().size()) {
        result = getColumnTranslators().get(column).translate(rs, column);
      }
      else {
        result = getValue(rs, column);
      }
    }
    catch (Exception e) {
      logColumnErrors(column, e);
    }
    return StringUtils.defaultIfEmpty(result, "");
  }

  protected void logColumnErrors(int column, Exception e) {
    if (isDisplayColumnErrors()) {
      log.debug("Unable to retrieve data item " + column, e);
    }
  }

  protected void updateMetadataQueryCount(AdaptrisMessage message, long numResults) {
    if(!StringUtils.isEmpty(getResultCountMetadataItem())) {
      updateMetadata(message, numResults, getResultCountMetadataItem());
    }
  }

  protected void updateMetadataUpdateCount(AdaptrisMessage message, JdbcResult jdbcResult) {
    if(!StringUtils.isEmpty(getUpdateCountMetadataItem())) {
      updateMetadata(message, jdbcResult.getNumRowsUpdated(), getUpdateCountMetadataItem());
    }
  }

  protected void updateMetadata(AdaptrisMessage message, long numResults, String metadataItemName) {
    message.addMessageHeader(metadataItemName, String.valueOf(numResults));
  }

  /**
   * @return whether to log errors caused by problem data
   */
  public Boolean getDisplayColumnErrors() {
    return displayColumnErrors;
  }

  /**
   * Sets whether to log any errors encountered when retrieving fields from the database.
   * <p>
   * As an example, some databases use "0000-00-00" instead of a null date. This will cause an exception when attempting to retrieve
   * the date using JDBC. We ignore the exception and simply produce an empty element; setting this flag causes the exception to be
   * logged (handy during testing)
   * </p>
   *
   * @param b
   */
  public void setDisplayColumnErrors(Boolean b) {
    displayColumnErrors = b;
  }

  protected boolean isDisplayColumnErrors() {
    return BooleanUtils.toBooleanDefaultIfNull(getDisplayColumnErrors(), false);
  }

  public List<ColumnTranslator> getColumnTranslators() {
    return columnTranslators;
  }

  public <T extends ResultSetTranslatorImp> T withColumnTranslators(
      ColumnTranslator... columnTranslators) {
    return withColumnTranslators(new ArrayList<>(Arrays.asList(columnTranslators)));
  }

  public <T extends ResultSetTranslatorImp> T withColumnTranslators(
      List<ColumnTranslator> translators) {
    setColumnTranslators(translators);
    return (T) this;
  }

  /**
   * Set the list of column translators that will be used and applied against each column in the result set.
   * <p>
   * If this list is not empty then each translator in the list will be used to translate the corresponding column in the result
   * set. If the list is empty then each column in the result set will be treated as either a byte[] or String column which may lead
   * to undefined behaviour in the event of columns being CLOB / NCLOB / BLOB types.
   * </p>
   *
   * @param list default is empty.
   */
  public void setColumnTranslators(List<ColumnTranslator> list) {
    columnTranslators = Args.notNull(list, "columnTranslators");
  }

  public void addColumnTranslator(ColumnTranslator ct) {
    columnTranslators.add(Args.notNull(ct, "columnTranslator"));
  }

  public String getResultCountMetadataItem() {
    return resultCountMetadataItem;
  }

  /**
   * Add the number of resultsets to metadata.
   *
   * @param s the metadata to add the value against; default is null (no output)
   */
  public void setResultCountMetadataItem(String s) {
    resultCountMetadataItem = s;
  }

  public String getUpdateCountMetadataItem() {
    return updateCountMetadataItem;
  }

  /**
   * Add the number of result sets updated to metadata.
   *
   * @param s the metadata to add the value against; default is null (no output)
   */
  public void setUpdateCountMetadataItem(String s) {
    updateCountMetadataItem = s;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   *
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * Not required as this component doesn't need to extend {@link AdaptrisComponent}
   *
   * @deprecated since 3.6.3
   */
  @Deprecated
  @Removal(version = "3.9.0")
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }
}
