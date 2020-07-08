package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationCheckReport {

  private String checkName;
    
  private List<String> warnings;
  
  private List<Exception> failureExceptions;
  
  public ConfigurationCheckReport() {
    this.setWarnings(new ArrayList<>());
    this.setFailureExceptions(new ArrayList<>());
  }

  public String getCheckName() {
    return checkName;
  }

  public void setCheckName(String checkName) {
    this.checkName = checkName;
  }

  public boolean isCheckPassed() {
    return this.getWarnings().size() == 0 && this.getFailureExceptions().size() == 0;
  }

  public List<Exception> getFailureExceptions() {
    return failureExceptions;
  }

  public void setFailureExceptions(List<Exception> failureExceptions) {
    this.failureExceptions = failureExceptions;
  }
  
  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append(this.getCheckName());
    buffer.append(": ");
    if(this.isCheckPassed())
      buffer.append("\nPassed.");
    if(this.getFailureExceptions().size() > 0) {
      buffer.append("\nFailed with exceptions: ");
      this.getFailureExceptions().forEach(exception -> {
        buffer.append("\n" + exception.getMessage());
      });
    }
    if(this.getWarnings().size() > 0) {
      buffer.append("\nWarnings found;");
      this.getWarnings().forEach(warningText -> {
        buffer.append("\n" + warningText);
      });
    }
    
    return buffer.toString();
  }
  
}
