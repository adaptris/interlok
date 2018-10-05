package com.adaptris.validation.constraints;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.Test;

public class BooleanExpressionValidatorTest {
  private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

  @Test
  public void testValidTrue() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "true";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidTrueCapital() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "TRUE";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidFalse() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "false";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidFalseCapital() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "FALSE";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidExpression() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "%message{key}";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testNull() {
    TestBean testBean = new TestBean();
    testBean.booleanField = null;
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testEmptyString() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testInvalid() {
    TestBean testBean = new TestBean();
    testBean.booleanField = "NotABoolean";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals("{com.adaptris.validation.constraints.BooleanExpression.message}", constraintViolation.getMessageTemplate());
  }

  public static class TestBean {
    @BooleanExpression
    public String booleanField;
  }

}
