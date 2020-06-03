package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationCheckReport {

  private String checkName;
    
  private List<String> warnings;
  
  private Exception failureException;
  
  public ConfigurationCheckReport() {
    this.setWarnings(new ArrayList<>());
  }

  public String getCheckName() {
    return checkName;
  }

  public void setCheckName(String checkName) {
    this.checkName = checkName;
  }

  public boolean isCheckPassed() {
    return this.getWarnings().size() == 0 && this.getFailureException() == null;
  }

  public Exception getFailureException() {
    return failureException;
  }

  public void setFailureException(Exception failureException) {
    this.failureException = failureException;
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
    if(this.getFailureException() != null) {
      buffer.append("\nFailed with exception: ");
      buffer.append(this.getFailureException().getMessage());
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
