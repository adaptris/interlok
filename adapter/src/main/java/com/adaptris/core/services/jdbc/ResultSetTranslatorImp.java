package com.adaptris.core.services.jdbc;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.jdbc.types.ColumnTranslator;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Base implementation for converting a {@linkplain ResultSet} into an {@linkplain AdaptrisMessage}.
 * 
 * @author lchan
 * 
 */
public abstract class ResultSetTranslatorImp implements ResultSetTranslator {

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

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private Boolean displayColumnErrors = null;
  @NotNull
  @AutoPopulated
  private ColumnStyle columnNameStyle;
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private List<ColumnTranslator> columnTranslators;

  protected ResultSetTranslatorImp() {
    setColumnNameStyle(ColumnStyle.NoStyle);
    setColumnTranslators(new ArrayList<ColumnTranslator>());
  }

  @Override
  public void close() {
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Standard);
  }

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
    if (result == null) {
      result = "";
    }
    return result;
  }

  protected void logColumnErrors(int column, Exception e) {
    if (isDisplayColumnErrors()) {
      log.debug("Unable to retrieve data item " + column, e);
    }
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
    return displayColumnErrors == null ? false : displayColumnErrors.booleanValue();
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

  public List<ColumnTranslator> getColumnTranslators() {
    return columnTranslators;
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
    if (list == null) {
      throw new IllegalArgumentException("List Column Translator may not be null");
    }
    columnTranslators = list;
  }

  public void addColumnTranslator(ColumnTranslator ct) {
    if (ct == null) {
      throw new IllegalArgumentException("Column Translator may not be null");
    }
    columnTranslators.add(ct);
  }
}
