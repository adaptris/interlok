package com.adaptris.core.jdbc;

import com.adaptris.util.text.NullConverter;
import com.adaptris.util.text.NullPassThroughConverter;

public abstract class NullableParameter extends AbstractParameter {

  private NullConverter nullConverter;

  public NullableParameter() {
    super();
  }

  public NullConverter getNullConverter() {
    return nullConverter;
  }

  /**
   * Set the {@link NullConverter} implementation to use.
   * 
   * @param nc the null converter implementation (default is {@link NullPassThroughConverter}).
   */
  public void setNullConverter(NullConverter nc) {
    this.nullConverter = nc;
  }

  NullConverter nullConverter() {
    return getNullConverter() != null ? getNullConverter() : new NullPassThroughConverter();
  }

  protected <T> T normalize(T obj) {
    return nullConverter().convert(obj);
  }
}
