package com.adaptris.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * {@code BooleanExpression} is a field/method/parameter level constraint which can be applied on a
 * string to assert that the string represents a valid Boolean or a message expression
 * <em>%message{key}</em>.<br/>
 * The valid values are:
 * <ul>
 * <li>true</li>
 * <li>false</li>
 * <li>True</li>
 * <li>False</li>
 * <li>TRUE</li>
 * <li>FALSE</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Constraint(validatedBy = BooleanExpressionValidator.class)
public @interface BooleanExpression {

  String message() default "{com.adaptris.validation.constraints.BooleanExpression.message}";

  String pattern() default ExpressionValidator.DEFAULT_EXPRESSION_PATTERN;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
