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

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.AutoPopulated;

/**
 * Base implementation for converting a {@linkplain java.sql.ResultSet} into an {@linkplain com.adaptris.core.AdaptrisMessage}.
 * 
 */
public abstract class StyledResultTranslatorImp extends ResultSetTranslatorBase {

  /**
   * Represents how column names are formatted.
   * 
   * 
   */
  public enum ColumnStyle {
    LowerCase {
      @Override
      public String format(String s) {
        return s.toLowerCase();
      }
    },
    UpperCase {
      @Override
      public String format(String s) {
        return s.toUpperCase();
      }
    },
    Capitalize {
      @Override
      public String format(String s) {
        return StringUtils.capitalize(s);
      }
    },
    NoStyle {
      @Override
      public String format(String s) {
        return s;
      }
    };
    public abstract String format(String s);
  };


  @NotNull
  @AutoPopulated
  private ColumnStyle columnNameStyle;

  protected StyledResultTranslatorImp() {
    setColumnNameStyle(ColumnStyle.NoStyle);
  }

  /**
   * Get the column name format.
   *
   * @return the format.
   */
  public ColumnStyle getColumnNameStyle() {
    return columnNameStyle;
  }

  /**
   * Format the column name in a specific style.
   *
   * @param style the style, valid values are UpperCase, LowerCase, Capitalize, NoStyle.
   * @see ColumnStyle
   */
  public void setColumnNameStyle(ColumnStyle style) {
    columnNameStyle = style;
  }
}
