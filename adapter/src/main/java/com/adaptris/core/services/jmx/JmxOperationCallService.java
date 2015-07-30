package com.adaptris.core.services.jmx;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.security.password.Password;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Allows you to make a remote call on a JMX operation.
 * </p>
 * <p>
 * You can set parameters for the call using {@link ValueTranslator}'s and also a single
 * {@link ValueTranslator} to help translate the result back into the Message.
 * </p>
 * <p>
 * If you do not wish to translate the result of the operation, simply omit the
 * "result-value-translator".
 * </p>
 * 
 * @since 3.0.3
 * 
 * @config jmx-operation-call-service
 * @since 3.0.3
 */
@XStreamAlias("jmx-operation-call-service")
public class JmxOperationCallService extends ServiceImp {
  
  /**
   * Should you want to translate the result of the operation back into the message, configure a single {@link ValueTranslator}.
   */
  @AutoPopulated
  private ValueTranslator resultValueTranslator;

  /**
   * The full JMX service URL which will point to the instance of the JMX server of your chosen Interlok instance.
   */
  @NotBlank
  private String jmxServiceUrl;
  
  /**
   * The fully qualified object name string, pointing to the object containing your chosen operation.
   */
  @NotBlank
  private String objectName;
  
  /**
   * The name of the operation that belongs to the ObjectName specified by the jmx-service-url.
   */
  @NotBlank
  private String operationName;
  
  /**
   * Your JMX username.
   */
  private String username;
  
  /**
   * JMX password.  Also supports encoded passwords with {@link Password}
   */
  private String password;
  
  private List<ValueTranslator> operationParameters;
  
  /**
   * Internal use only.
   */
  private transient JmxOperationInvoker invoker;
  
  public JmxOperationCallService() {
    this.setResultValueTranslator(null);
    this.setInvoker(new JmxOperationInvoker());
    this.setOperationParameters(new ArrayList<ValueTranslator>());
  }

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      Object result = this.getInvoker().invoke(
          this.getJmxServiceUrl(), 
          this.getObjectName(),
          this.getUsername(), 
          this.getPassword(), 
          this.getOperationName(), 
          this.parametersToArray(message), 
          this.parametersToTypeArray(message));
      
      if(this.getResultValueTranslator() != null)
        this.getResultValueTranslator().setValue(message, result);
      
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }
  
  private Object[] parametersToArray(AdaptrisMessage message) throws CoreException {
    Object[] returnArray = new Object[this.getOperationParameters().size()];
    for(int count = 0; count < this.getOperationParameters().size(); count ++)
      returnArray[count] = this.getOperationParameters().get(count).getValue(message);
    
    return returnArray;
  }
  
  private String[] parametersToTypeArray(AdaptrisMessage message) {
    String[] returnArray = new String[this.getOperationParameters().size()];
    for(int count = 0; count < this.getOperationParameters().size(); count ++)
      returnArray[count] = this.getOperationParameters().get(count).getType();
    
    return returnArray;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void close() {
  }

  public String getJmxServiceUrl() {
    return jmxServiceUrl;
  }

  public void setJmxServiceUrl(String jmxServiceUrl) {
    this.jmxServiceUrl = jmxServiceUrl;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ValueTranslator getResultValueTranslator() {
    return resultValueTranslator;
  }

  public void setResultValueTranslator(ValueTranslator resultValueTranslator) {
    this.resultValueTranslator = resultValueTranslator;
  }

  JmxOperationInvoker getInvoker() {
    return invoker;
  }

  void setInvoker(JmxOperationInvoker invoker) {
    this.invoker = invoker;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public List<ValueTranslator> getOperationParameters() {
    return operationParameters;
  }

  public void setOperationParameters(List<ValueTranslator> parameters) {
    this.operationParameters = parameters;
  }

}
