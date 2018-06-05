package com.adaptris.validation.constraints;

public class NumberExpressionValidator extends ExpressionValidator<NumberExpression> {

  private final String NUMBER_PATTERN = "[+-]?([0-9]*[.])?[0-9]+";

  @Override
  public void initialize(NumberExpression constraintAnnotation) {
    setPattern(constraintAnnotation.pattern());
  }

  @Override
  public boolean onIsValid(String value) {
    return isNumber(value);
  }

  private boolean isNumber(String value) {
    return value.matches(NUMBER_PATTERN);
  }

}
