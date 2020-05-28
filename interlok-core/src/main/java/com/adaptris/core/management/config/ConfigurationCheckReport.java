package com.adaptris.core.management.config;

public class ConfigurationCheckReport {

  private String checkName;
  
  private boolean checkPassed;
  
  private Exception failureException;
  
  public ConfigurationCheckReport() {
    
  }

  public String getCheckName() {
    return checkName;
  }

  public void setCheckName(String checkName) {
    this.checkName = checkName;
  }

  public boolean isCheckPassed() {
    return checkPassed;
  }

  public void setCheckPassed(boolean checkPassed) {
    this.checkPassed = checkPassed;
  }

  public Exception getFailureException() {
    return failureException;
  }

  public void setFailureException(Exception failureException) {
    this.failureException = failureException;
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append(this.getCheckName());
    buffer.append(": ");
    buffer.append(this.isCheckPassed() ? "Passed." : "Failed.");
    
    if(this.getFailureException() != null) {
      buffer.append("  With Exception: ");
      buffer.append(this.getFailureException().getMessage());
    }
    
    return buffer.toString();
  }
  
}
