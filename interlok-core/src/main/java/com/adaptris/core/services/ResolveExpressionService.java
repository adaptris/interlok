package com.adaptris.core.services;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("resolve-expression-service")
@AdapterComponent
@ComponentProfile(summary = "Will perform an expression resolve on any input and put the result into the output.", tag = "service,resolve,expression")
public class ResolveExpressionService extends ServiceImp {

  @Getter
  @Setter
  private DataInputParameter<String> input;
  
  @Getter
  @Setter
  private DataOutputParameter<String> output;
  
  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      getOutput().insert(message.resolve(getInput().extract(message)), message);
    } catch (Throwable e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {
  }

}
