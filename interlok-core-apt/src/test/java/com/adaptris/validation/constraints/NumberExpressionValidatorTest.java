package com.adaptris.validation.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

public class NumberExpressionValidatorTest {
  private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

  @Test
  public void testValidPositive() {
    TestBean testBean = new TestBean();
    testBean.numberField = "123456789";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidPositiveFloat() {
    TestBean testBean = new TestBean();
    testBean.numberField = "1.23456789";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidNegative() {
    TestBean testBean = new TestBean();
    testBean.numberField = "-123456789";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidNegativeFloat() {
    TestBean testBean = new TestBean();
    testBean.numberField = "-1.23456789";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidExpression() {
    TestBean testBean = new TestBean();
    testBean.numberField = "%message{key}";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testNull() {
    TestBean testBean = new TestBean();
    testBean.numberField = null;
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testEmptyString() {
    TestBean testBean = new TestBean();
    testBean.numberField = "";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testInvalid() {
    TestBean testBean = new TestBean();
    testBean.numberField = "NotANumber";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    assertEquals("{com.adaptris.validation.constraints.NumberExpression.message}", constraintViolation.getMessageTemplate());
  }

  public static class TestBean {
    @NumberExpression
    public String numberField;
  }

}
