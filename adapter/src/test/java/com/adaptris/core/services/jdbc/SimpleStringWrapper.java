package com.adaptris.core.services.jdbc;

public class SimpleStringWrapper {

  private String value;

  private SimpleStringWrapper() {

  }

  public SimpleStringWrapper(String s) {
    this();
    value = s;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SimpleStringWrapper) {
      return value.equals(((SimpleStringWrapper) o).value);
    }
    return false;
  }

  @Override
  public String toString() {
    return value;
  }
}
