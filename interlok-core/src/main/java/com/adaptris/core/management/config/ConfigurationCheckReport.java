package com.adaptris.core.management.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationCheckReport {

  private String checkName;
  private String checkerClassName;

  private List<String> warnings;

  private List<Exception> failureExceptions;

  public ConfigurationCheckReport() {
    setWarnings(new ArrayList<>());
    setFailureExceptions(new ArrayList<>());
  }

  public String getCheckName() {
    return checkName;
  }

  public void setCheckName(String checkName) {
    this.checkName = checkName;
  }

  public String getCheckClassName() {
    return checkerClassName;
  }

  public void setCheckClassName(String checkerClassName) {
    this.checkerClassName = checkerClassName;
  }

  public boolean isCheckPassed() {
    return getWarnings().isEmpty() && getFailureExceptions().isEmpty();
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

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append(getCheckName());
    buffer.append(": ");
    if(isCheckPassed()) {
      buffer.append("\nPassed.");
    }
    if(getFailureExceptions().size() > 0) {
      buffer.append("\nFailed with exceptions: ");
      getFailureExceptions().forEach(exception -> {
        buffer.append("\n" + exception.getMessage());
      });
    }
    if(getWarnings().size() > 0) {
      buffer.append("\nWarnings found;");
      getWarnings().forEach(warningText -> {
        buffer.append("\n" + warningText);
      });
    }

    return buffer.toString();
  }

}
