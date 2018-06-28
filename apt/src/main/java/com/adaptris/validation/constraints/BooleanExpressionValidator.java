package com.adaptris.validation.constraints;

public class BooleanExpressionValidator extends ExpressionValidator<BooleanExpression> {

  @Override
  public void initialize(BooleanExpression constraintAnnotation) {
    setPattern(constraintAnnotation.pattern());
  }

  @Override
  public boolean onIsValid(String value) {
    return isBoolean(value);
  }

  private boolean isBoolean(String value) {
    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
  }

}
