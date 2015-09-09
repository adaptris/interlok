package com.adaptris.core.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service which validates an input XML document.
 * 
 * @config xml-validation-service
 * @license BASIC
 */
@XStreamAlias("xml-validation-service")
public class XmlValidationService extends ServiceImp {

  @NotNull
  @Valid
  private List<MessageValidator> validators;

  public XmlValidationService() {
    super();
    setValidators(new ArrayList<MessageValidator>());
  }

  public XmlValidationService(MessageValidator... validators) {
    this();
    setValidators(new ArrayList(Arrays.asList(validators)));
  }

  public void init() throws CoreException {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.init(v);
    }
  }

  public void start() throws CoreException {
    super.start();
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.start(v);
    }
  }

  public void stop() {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.stop(v);
    }
    super.stop();
  }

  public void close() {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.close(v);
    }
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for (MessageValidator v : getValidators()) {
        v.validate(msg);
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    int expected = validators.size() + 1;
    int count = 0;
    count += license.isEnabled(LicenseType.Basic) ? 1 : 0;
    for (MessageValidator v : getValidators()) {
      count += v.isEnabled(license) ? 1 : 0;
    }
    return expected == count;
  }

  public List<MessageValidator> getValidators() {
    return validators;
  }

  public void setValidators(List<MessageValidator> validators) {
    if (validators == null) throw new IllegalArgumentException("Validators may not be null");
    this.validators = validators;
  }
}
