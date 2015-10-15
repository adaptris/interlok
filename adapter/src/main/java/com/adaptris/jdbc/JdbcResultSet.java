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

import java.util.ArrayList;
import java.util.List;

public class JdbcResultSet {

  private List<JdbcResultRow> rows;
  
  public JdbcResultSet() {
    this.setRows(new ArrayList<JdbcResultRow>());
  }

  public List<JdbcResultRow> getRows() {
    return rows;
  }

  public void setRows(List<JdbcResultRow> rows) {
    this.rows = rows;
  }

  public void addRow(JdbcResultRow row) {
    this.getRows().add(row);
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(this.getClass().getSimpleName() + " " + this.getRows().size() + " rows\n");
    
    for(JdbcResultRow row : this.getRows()) {
      buffer.append("\t\t" + row.toString() + "\n");
    }
    
    return buffer.toString();
  }
}
