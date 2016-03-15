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

package com.adaptris.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class JdbcResultSet {

  private ResultSet resultSet;
  
  public JdbcResultSet(ResultSet resultSet) {
    this.resultSet = resultSet;
  }
  
  public Iterable<JdbcResultRow> getRows() {
    return new Iterable<JdbcResultRow>() {

      @Override
      public Iterator<JdbcResultRow> iterator() {
        try {
          switch(resultSet.getType()) {
          case ResultSet.TYPE_FORWARD_ONLY:
            return new ForwardOnlyResultSetIterator(resultSet);
            
          case ResultSet.TYPE_SCROLL_SENSITIVE:
          case ResultSet.TYPE_SCROLL_INSENSITIVE:
            return new ScrollableResultSetIterator(resultSet);
            
          default:
            throw new RuntimeException("ResultSet was of unknown type");
          }
        } catch(SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
  
  /**
   * ScrollableResultSetIterator works only for ResultSets that support .isAfterLast() but
   * follows the ResultSet API most closely by not 'pre-taking' any elements until next()
   * is called.
   */
  private static class ScrollableResultSetIterator implements Iterator<JdbcResultRow> {
    private final ResultSet resultSet;
    
    private ScrollableResultSetIterator(ResultSet resultSet) {
      this.resultSet = resultSet;
    }
    
    @Override
    public boolean hasNext() {
      try {
        return !resultSet.isAfterLast();
      } catch (SQLException e) {
        return false;
      }
    }

    @Override
    public JdbcResultRow next() {
      try {
        if(resultSet.next()) {
          return mapRow(resultSet);
        } else {
          throw new NoSuchElementException();
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  /**
   * ForwardOnlyResultSetIterator works for ResultSets that don't support .isAfterLast(), which
   * is optional for TYPE_FORWARD_ONLY ResultSets. It works by 'pre-taking' an element and hasNext()
   * reports whether that was successful. A side effect of that is that constructing this ResultSet
   * can throw an Exception. 
   */
  private static class ForwardOnlyResultSetIterator implements Iterator<JdbcResultRow> {
    private final ResultSet resultSet;
    private JdbcResultRow nextRow;
    
    private ForwardOnlyResultSetIterator(ResultSet resultSet) {
      this.resultSet = resultSet;
      nextRow = takeNext();
    }
    
    @Override
    public boolean hasNext() {
      return nextRow != null;
    }

    @Override
    public JdbcResultRow next() {
      if(hasNext()) {
        JdbcResultRow tmp = nextRow;
        nextRow = takeNext();
        return tmp;
      } else {
        throw new NoSuchElementException();
      }
    }
    
    private JdbcResultRow takeNext() {
      try {
        if(resultSet.next()) {
          return mapRow(resultSet);
        } else {
          return null;
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  private static JdbcResultRow mapRow(ResultSet resultSet) throws SQLException {
    ResultSetMetaData rsmd = resultSet.getMetaData();
    int columnCount = rsmd.getColumnCount();

    JdbcResultRow row = new JdbcResultRow();
    for(int counter = 1; counter <= columnCount; counter ++) {
      row.setFieldValue(rsmd.getColumnName(counter), resultSet.getObject(counter));
    }

    return row;
  }

}
