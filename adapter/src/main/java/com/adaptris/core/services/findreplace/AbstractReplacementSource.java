package com.adaptris.core.services.findreplace;

public abstract class AbstractReplacementSource implements ReplacementSource {

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
  
  public boolean equals(Object object) {
    if(this.getValue().equals(((AbstractReplacementSource) object).getValue()))
      return true;
    else
      return false;
  }
  
  public String toString() {
    return value;
  }
}
