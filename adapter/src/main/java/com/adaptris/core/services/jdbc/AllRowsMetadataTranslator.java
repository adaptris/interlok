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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Translate the all the rows of the result set into metadata.
 * 
 * <p>
 * Each column of the result set is used to create a new item of metadata. The metadata key for each new metadata item is the
 * combination of {{@link #getMetadataKeyPrefix()}, {@link #getSeparator()}, the column name (or label if it's different) and a
 * counter. The value is the value of the column.
 * 
 * An additional note here; Some stored procedures can return multiple result sets, if this is the case then each metadata key will
 * be prefixed with the result set counter, plus the {@link #getResultSetCounterPrefix()} If there is only 1 result set, no prefix
 * will be applied for the result set count.
 * </p>
 * <p>
 * The counter starts from 1
 * </p>
 * <p>
 * If you want to see how many rows were processed you can set one/both of the following;
 * <table>
 * <tr>
 * <th>Item</th>
 * <th>Description</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>result-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows returned by your query</td><td>Metadata item name</td>
 * </tr>
 * <tr>
 * <td>update-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows updated by your SQL statement</td><td>Metadata item name</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @config jdbc-all-rows-metadata-translator
 * 
 * 
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-all-rows-metadata-translator")
@DisplayOrder(order = {"metadataKeyPrefix", "separator", "columnNameStyle", "columnTranslators", "resultSetCounterPrefix",
    "rowTotalMetadataKey"})
public class AllRowsMetadataTranslator extends MetadataResultSetTranslatorImpl {

  /**
   * @deprecated
   * Use result-count-metadata-item to specify the key to which the result count will be set.
   */
  @Deprecated
  private String rowTotalMetadataKey;

  public AllRowsMetadataTranslator() {
    super();
  }

  @Override
  public void translateResult(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {

    List<MetadataElement> added = new ArrayList<MetadataElement>();
    int resultSetCount = 0;
    for (JdbcResultSet resultSet : source.getResultSets()) {
      int counter = 0;
      String resultSetPrefix = source.countResultSets() > 1 ? Integer.toString(resultSetCount) + getResultSetCounterPrefix() : "";
      for (JdbcResultRow row : resultSet.getRows()) {
        for (int i = 0; i < row.getFieldCount(); i++) {
          MetadataElement md = new MetadataElement(resultSetPrefix + getMetadataKeyPrefix() + getSeparator()
              + getColumnNameStyle().format(row.getFieldName(i)) + getSeparator() + counter, toString(row, i));
          if (log.isTraceEnabled()) {
            added.add(md);
          }
          target.addMetadata(md);
        }

        counter++;
      }
      if (!isEmpty(getRowTotalMetadataKey())) {
        target.addMetadata(resultSetPrefix + getRowTotalMetadataKey(), String.valueOf(counter));
      }      
      resultSetCount++;
    }

    if (log.isTraceEnabled()) {
      log.debug("Added metadata : " + added);
    }
  }

  public String getRowTotalMetadataKey() {
    return rowTotalMetadataKey;
  }

  /**
   * Specify the metadata key which will contain the total number of rows converted.
   * 
   * @param key the metadata key.
   */
  public void setRowTotalMetadataKey(String key) {
    this.rowTotalMetadataKey = key;
  }
}
