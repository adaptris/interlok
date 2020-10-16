/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
