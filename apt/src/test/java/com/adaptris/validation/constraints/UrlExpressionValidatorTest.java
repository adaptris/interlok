package com.adaptris.validation.constraints;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.Test;

public class UrlExpressionValidatorTest {
  private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

  @Test
  public void testValidProtocolAndHost() {
    TestBean testBean = new TestBean();
    testBean.urlField = "http://host";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidProtocolAndHostAndPort() {
    TestBean testBean = new TestBean();
    testBean.urlField = "http://host:80";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidSpecifiedProtocol() {
    TestBean testBean = new TestBean();
    testBean.urlFieldProtocol = "https://host";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidSpecifiedHost() {
    TestBean testBean = new TestBean();
    testBean.urlFieldHost = "http://myhost";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidSpecifiedPort() {
    TestBean testBean = new TestBean();
    testBean.urlFieldPort = "http://host:443";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidExpression() {
    TestBean testBean = new TestBean();
    testBean.urlField = "%message{key}";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testNull() {
    TestBean testBean = new TestBean();
    testBean.urlField = null;
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testEmptyString() {
    TestBean testBean = new TestBean();
    testBean.urlField = "";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testInvalid() {
    TestBean testBean = new TestBean();
    testBean.urlField = "NotAUrl";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals("{com.adaptris.validation.constraints.UrlExpression.message}", constraintViolation.getMessageTemplate());
  }

  @Test
  public void testInalidSpecifiedProtocol() {
    TestBean testBean = new TestBean();
    testBean.urlFieldProtocol = "http://host";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals("{com.adaptris.validation.constraints.UrlExpression.message}", constraintViolation.getMessageTemplate());
  }

  @Test
  public void testInalidSpecifiedHost() {
    TestBean testBean = new TestBean();
    testBean.urlFieldHost = "http://host";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals("{com.adaptris.validation.constraints.UrlExpression.message}", constraintViolation.getMessageTemplate());
  }

  @Test
  public void testInalidSpecifiedPort() {
    TestBean testBean = new TestBean();
    testBean.urlFieldPort = "http://host:80";
    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);
    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals("{com.adaptris.validation.constraints.UrlExpression.message}", constraintViolation.getMessageTemplate());
  }

  public static class TestBean {
    @UrlExpression
    public String urlField;
    @UrlExpression(protocol = "https")
    public String urlFieldProtocol;
    @UrlExpression(host = "myhost")
    public String urlFieldHost;
    @UrlExpression(port = 443)
    public String urlFieldPort;
  }

}
