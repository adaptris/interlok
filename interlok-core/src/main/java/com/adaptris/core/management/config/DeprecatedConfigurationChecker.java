package com.adaptris.core.management.config;

import static com.adaptris.core.util.LoggingHelper.filterGuid;
import static com.adaptris.core.util.LoggingHelper.reflectiveUniqueID;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import com.adaptris.core.Adapter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeprecatedConfigurationChecker extends ValidationCheckerImpl {

  private static final String FRIENDLY_NAME = "Deprecated checks";
  private static final ValidatorFactory JAVAX_VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();


  @Override
  protected void validate(Adapter adapter, ConfigurationCheckReport report) {
    try {
      Validator validator = JAVAX_VALIDATOR_FACTORY.getValidator();
      report.setWarnings(violationsToWarning(validator.validate(adapter, Deprecated.class)));
    } catch (Exception ex) {
      report.getFailureExceptions().add(ex);
    }
  }

  @Override
  public String getFriendlyName() {
    return FRIENDLY_NAME;
  }

  private List<String> violationsToWarning(Set<ConstraintViolation<Adapter>> violations) {
    return violations.stream()
        .filter((v) -> !isListImpl(v.getPropertyPath().toString()))
        .map(v -> String.format("Interlok Deprecation Warning: [%1$s][%2$s]: %3$s",
            v.getPropertyPath(), friendlyName(v.getLeafBean()), v.getMessage()))
        .collect(Collectors.toList());
  }

  public static String friendlyName(Object o) {
    return Optional.ofNullable(o)
        .map((obj) -> obj.getClass().getSimpleName() + filterGuid(reflectiveUniqueID(obj)))
        .orElse("");
  }
}
