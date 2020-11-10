package com.adaptris.core.management.config;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.core.Adapter;

public class DeprecatedConfigurationChecker extends AdapterConfigurationChecker {

  private static final String PATTERN_WORKFLOW_LIST_ARRAY = ".*workflowList\\[[0-9+]\\].*";
  private static final String PATTERN_CHANNEL_LIST_ARRAY = ".*channelList\\[[0-9+]\\].*";
  private static final String FRIENDLY_NAME = "Deprecated checks";
  private static final ValidatorFactory JAVAX_VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

  private transient Pattern isWorkflowListArray;
  private transient Pattern isChannelListArray;

  public DeprecatedConfigurationChecker() {
    isWorkflowListArray = Pattern.compile(PATTERN_WORKFLOW_LIST_ARRAY);
    isChannelListArray = Pattern.compile(PATTERN_CHANNEL_LIST_ARRAY);
  }

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
        .map(v -> String.format("Interlok Deprecation Warning: [%1$s] is deprecated. %2$s", v.getPropertyPath(), v.getMessage()))
        .collect(Collectors.toList());
  }

  // Since ChannelList & workflowList implements collection, we have
  // the weird situation where a single "issue" can trigger 4 separate warnings.
  private boolean isListImpl(String path) {
    return BooleanUtils.or(new boolean[] {
        isChannelListArray.matcher(path).matches(),
        isWorkflowListArray.matcher(path).matches()
    });

  }
}
