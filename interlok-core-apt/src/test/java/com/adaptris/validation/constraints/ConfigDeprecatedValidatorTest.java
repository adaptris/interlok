package com.adaptris.validation.constraints;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.Test;

public class ConfigDeprecatedValidatorTest {
  private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

  @Test
  public void testValidConfig() {
    TestBean testBean = new TestBean();

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidHasDeprecatedGroupNotProvided() {
    TestBean testBean = new TestBean();
    testBean.deprecatedString = "value";

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean);

    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidHasDeprecated() {
    TestBean testBean = new TestBean();
    testBean.deprecatedString = "value";

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals(ConfigDeprecated.MESSAGE_TEMPLATE, constraintViolation.getMessageTemplate());
    Assert.assertEquals("Is deprecated. It will be removed in " + ConfigDeprecated.DEFAULT_VERSION, constraintViolation.getMessage());
  }

  @Test
  public void testValidHasDeprecatedWithVersion() {
    TestBean testBean = new TestBean();
    testBean.deprecatedStringWithVersion = "value";

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals(ConfigDeprecated.MESSAGE_TEMPLATE, constraintViolation.getMessageTemplate());
    Assert.assertEquals("Is deprecated. It will be removed in 4.0.0", constraintViolation.getMessage());
  }

  @Test
  public void testValidHasDeprecatedNoMessage() {
    TestBean testBean = new TestBean();
    testBean.deprecatedStringNoMessage = "value";

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals(ConfigDeprecated.MESSAGE_TEMPLATE, constraintViolation.getMessageTemplate());
    Assert.assertEquals("Is deprecated. It will be removed in " + ConfigDeprecated.DEFAULT_VERSION, constraintViolation.getMessage());
  }

  @Test
  public void testValidConfigWithNestedObject() {
    TestBean testBean = new TestBean();
    testBean.object = new TestBean();

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidHasDeprecatedNestedObject() {
    TestBean testBean = new TestBean();
    testBean.object = new DeprecatedTestBean();

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals(ConfigDeprecated.MESSAGE_TEMPLATE, constraintViolation.getMessageTemplate());
  }

  @Test
  public void testValidConfigWithNestedListObject() {
    TestBean testBean = new TestBean();
    testBean.objects = Arrays.asList(new TestBean());

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertTrue(constraintViolations.isEmpty());
  }

  @Test
  public void testValidHasDeprecatedNestedListObject() {
    TestBean testBean = new TestBean();
    testBean.objects = Arrays.asList(new DeprecatedTestBean());

    Set<ConstraintViolation<TestBean>> constraintViolations = validatorFactory.getValidator().validate(testBean, Deprecated.class);

    Assert.assertEquals(1, constraintViolations.size());
    ConstraintViolation<TestBean> constraintViolation = constraintViolations.iterator().next();
    Assert.assertEquals(ConfigDeprecated.MESSAGE_TEMPLATE, constraintViolation.getMessageTemplate());
  }

  public interface TestInterface {
  }

  public static class TestBean implements TestInterface {
    String string;
    @ConfigDeprecated(groups = Deprecated.class)
    String deprecatedString;
    @ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
    String deprecatedStringWithVersion;
    @ConfigDeprecated(message = "", groups = Deprecated.class)
    String deprecatedStringNoMessage;
    @Valid
    TestInterface object;
    @Valid
    List<TestInterface> objects;
  }

  @ConfigDeprecated(groups = Deprecated.class)
  public static class DeprecatedTestBean implements TestInterface {
  }

}
