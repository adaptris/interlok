package com.adaptris.validation.constraints;



import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public abstract class ExpressionValidator<A extends Annotation> implements ConstraintValidator<A, String> {

  public static final String DEFAULT_EXPRESSION_PATTERN = "%message\\{([\\w!\\$\"#&'\\*\\+,\\-\\.:=]+)\\}";

  private String pattern;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value != null && !value.isEmpty()) {
      return onIsValid(value) || isExpression(value);
    }
    return true;
  }

  protected abstract boolean onIsValid(String value);


  protected final boolean isExpression(String value) {
    return value.matches(pattern);
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

}
