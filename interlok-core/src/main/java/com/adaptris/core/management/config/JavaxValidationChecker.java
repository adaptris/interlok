package com.adaptris.core.management.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import com.adaptris.core.Adapter;
import com.adaptris.core.CoreException;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JavaxValidationChecker extends ValidationCheckerImpl {

  private static final String FRIENDLY_NAME = "javax.validation checks";
  private static final ValidatorFactory JAVAX_VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

  @Override
  protected void validate(Adapter adapter, ConfigurationCheckReport report) {
    try {
      Validator validator = JAVAX_VALIDATOR_FACTORY.getValidator();
      // There are no warnings; everything is a hard failure.
      report.setFailureExceptions(violationsToException(validator.validate(adapter)));
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  private List<Exception> violationsToException(Set<ConstraintViolation<Adapter>> violations) {
    return violations.stream().filter((v) -> !isListImpl(v.getPropertyPath().toString()))
        .map(v -> new CoreException(String.format("Interlok Validation Error: [%1$s][%2$s]: %3$s",
            v.getPropertyPath(), friendlyName(v.getLeafBean()), v.getMessage())))
        .collect(Collectors.toList());
  }
}
