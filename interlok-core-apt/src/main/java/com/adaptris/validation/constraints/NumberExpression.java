package com.adaptris.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * {@code NumberExpression} is a field/method/parameter level constraint which can be applied on a
 * string to assert that the string represents a valid Number or a message expression
 * <em>%message{key}</em>. Positive, negative and float values are accepted
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Constraint(validatedBy = NumberExpressionValidator.class)
public @interface NumberExpression {

  String message() default "{com.adaptris.validation.constraints.NumberExpression.message}";

  String pattern() default ExpressionValidator.DEFAULT_EXPRESSION_PATTERN;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
