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
 * {@code UrlExpression} is a field/method/parameter level constraint which can be applied on a
 * string to assert that the string represents a valid URL or a message expression
 * <em>%message{key}</em>. Per default the constraint verifies that the annotated value conforms to
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC2396</a>. Via the parameters {@code protocol},
 * {@code host} and {@code port} one can assert the corresponding parts of the parsed URL. This
 * class has been inspired by <b>org.hibernate.validator.constraints.URL</b>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER})
@Constraint(validatedBy = UrlExpressionValidator.class)
public @interface UrlExpression {

  String message() default "{com.adaptris.validation.constraints.UrlExpression.message}";

  String pattern() default ExpressionValidator.DEFAULT_EXPRESSION_PATTERN;

  /**
   * @return the protocol (scheme) the annotated string must match, eg ftp or http. Per default any
   *         protocol is allowed
   */
  String protocol() default "";

  /**
   * @return the host the annotated string must match, eg localhost. Per default any host is allowed
   */
  String host() default "";

  /**
   * @return the port the annotated string must match, eg 80. Per default any port is allowed
   */
  int port() default -1;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
