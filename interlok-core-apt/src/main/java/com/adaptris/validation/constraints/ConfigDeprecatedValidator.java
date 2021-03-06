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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ConfigDeprecatedValidator implements ConstraintValidator<ConfigDeprecated, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    boolean isValid = value == null;

    if (!isValid && "".equals(context.getDefaultConstraintMessageTemplate())) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(ConfigDeprecated.MESSAGE_TEMPLATE)
      .addConstraintViolation();
    }

    return isValid;
  }

}
