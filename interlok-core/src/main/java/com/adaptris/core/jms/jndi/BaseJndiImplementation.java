package com.adaptris.core.jms.jndi;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsUtils;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.VendorImplementationBase;
import com.adaptris.core.jms.VendorImplementationImp;
import com.adaptris.core.util.Args;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

public abstract class BaseJndiImplementation extends VendorImplementationImp {

  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet jndiParams;
  @NotBlank
  protected String jndiName;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean useJndiForQueues;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean useJndiForTopics;
  protected transient volatile Context context = null;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean enableEncodedPasswords;
  @AdvancedConfig
  private String encodedPasswordKeys;
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private ExtraFactoryConfiguration extraFactoryConfiguration;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean newContextOnException;

  public BaseJndiImplementation() {
    super();
    setJndiParams(new KeyValuePairSet());
    setExtraFactoryConfiguration(new NoOpFactoryConfiguration());
  }

  protected Object lookup(String name) throws JMSException {
    Object result = null;
    try {
      if (context == null) {
        synchronized (this) {
          Properties p = KeyValuePairSet.asProperties(getJndiParams());

          List<String> encodedKeys = asList(getEncodedPasswordKeys(), ",");
          if (enableEncodedPasswords()) {
            encodedKeys.add(Context.SECURITY_CREDENTIALS);
          }
          for (String key : encodedKeys) {
            if (p.containsKey(key)) {
              p.setProperty(key, Password.decode(p.getProperty(key)));
            }
          }
          context = new InitialContext(p);
        }
      }
      result = context.lookup(name);
    }
    catch (Exception e) {
      if (newContextOnException()) {
        context = null;
      }
      JmsUtils.rethrowJMSException(e);
    }
    return result;
  }

  protected static List<String> asList(String commaSepList, String delim) {
    List<String> result = new ArrayList<String>();
    if (commaSepList != null) {
      StringTokenizer st = new StringTokenizer(commaSepList, delim);
      while (st.hasMoreTokens()) {
        result.add(st.nextToken());
      }
    }
    return result;
  }

  /**
   *
   * @see VendorImplementation#createQueue(java.lang.String, JmsActorConfig)
   *
   */
  @Override
  public Queue createQueue(String name, JmsActorConfig c) throws JMSException {
    Queue result = null;

    if (!useJndiForQueues()) {
      result = super.createQueue(name, c);
    }
    else {
      result = (Queue) lookup(name);
    }
    return result;
  }

  /**
   *
   * @see VendorImplementation#createTopic(java.lang.String, JmsActorConfig)
   */
  @Override
  public Topic createTopic(String name, JmsActorConfig c) throws JMSException {
    Topic result = null;
    if (!useJndiForTopics()) {
      result = super.createTopic(name, c);
    }
    else {
      result = (Topic) lookup(name);
    }

    return result;
  }

  /**
   * @see com.adaptris.core.jms.VendorImplementationImp #retrieveBrokerDetailsForLogging()
   */
  @Override
  public String retrieveBrokerDetailsForLogging() {
    // Filter out java.naming.security.credentials.
    Properties p = KeyValuePairBag.asProperties(getJndiParams());
    p.setProperty(Context.SECURITY_CREDENTIALS, "********");
    return "JNDI name=" + getJndiName() + " environment=" + p;
  }

  @Override
  public boolean connectionEquals(VendorImplementationBase vendorImp) {
    return retrieveBrokerDetailsForLogging().equals(vendorImp.retrieveBrokerDetailsForLogging());
  }

  /**
   * <p>
   * Returns the name to look up in the JNDI store.
   * </p>
   *
   * @return the name to look up in the JNDI store
   */
  public String getJndiName() {
    return jndiName;
  }

  /**
   * <p>
   * Sets the name to look up in the JNDI store. May not be null or empty.
   * </p>
   *
   * @param s the name to look up in the JNDI store that corresponds to a ConnectionFactory of the correct type.
   */
  public void setJndiName(String s) {
    jndiName = Args.notEmpty(s, "jndi name");
  }

  /**
   * <p>
   * Returns a <code>KeyValuePairSet</code> of the parameters requires to connect to the JNDI store. Keys are commonly represented
   * in Java by constants. It is the value of these constants not the constants themselves which should be configured in the
   * <code>KeyValuePairSet</code>.
   * </p>
   *
   * @return a <code>KeyValuePairSet</code> of the parameters requires to connect to the JNDI store
   */
  public KeyValuePairSet getJndiParams() {
    return jndiParams;
  }

  /**
   * <p>
   * Sets a <code>KeyValuePairSet</code> of the parameters requires to connect to the JNDI store. Keys are commonly represented in
   * Java by constants. It is the value of these constants not the constants themselves which should be configured in the
   * <code>KeyValuePairSet</code>. May not be null.
   * </p>
   *
   * @param k a <code>KeyValuePairSet</code> of the parameters requires to connect to the JNDI store
   */
  public void setJndiParams(KeyValuePairSet k) {
    jndiParams = Args.notNull(k, "jndi params");
  }

  /**
   * @return the useJndiForQueues
   * @see #setUseJndiForQueues(Boolean)
   */
  public Boolean getUseJndiForQueues() {
    return useJndiForQueues;
  }

  /**
   * Specify whether to use JNDI when attempting to create a Queue.
   * <p>
   * This specifies whether to use {@link Context#lookup(String)} to find any specified queues, if the named object is not present
   * within JNDI, then an Exception will be thrown
   * </p>
   *
   * @param b true to use JNDI to create a Queue, false to use standard JMS methods, default false.
   */
  public void setUseJndiForQueues(Boolean b) {
    useJndiForQueues = b;
  }

  public boolean useJndiForQueues() {
    return BooleanUtils.toBooleanDefaultIfNull(getUseJndiForQueues(), false);
  }

  /**
   * @return the useJndiForTopics
   * @see #setUseJndiForTopics(Boolean)
   */
  public Boolean getUseJndiForTopics() {
    return useJndiForTopics;
  }

  /**
   * Specify whether to use JNDI when attempting to create a Topic.
   * <p>
   * This specifies whether to use {@link Context#lookup(String)} to find any specified topics, if the named object is not present
   * within JNDI, then an Exception will be thrown
   * </p>
   *
   * @param b true to use JNDI to create a Topic, false to use standard JMS methods, default false.
   */
  public void setUseJndiForTopics(Boolean b) {
    useJndiForTopics = b;
  }

  public boolean useJndiForTopics() {
    return BooleanUtils.toBooleanDefaultIfNull(getUseJndiForTopics(), false);
  }

  /**
   * Whether or not encoded passwords are enabled.
   *
   * @return true if encoded passwords are to be supported.
   */
  public Boolean getEnableEncodedPasswords() {
    return enableEncodedPasswords;
  }

  /**
   * Specify whether or not to enable encoded passwords.
   * <p>
   * When enabled, the entry matching {@link Context#SECURITY_CREDENTIALS} will be parsed and decoding attempted using the
   * appropriate {@link com.adaptris.security.password.Password}
   * </p>
   *
   * @param b true to enable, false otherwise (default false)
   */
  public void setEnableEncodedPasswords(Boolean b) {
    enableEncodedPasswords = b;
  }

  public boolean enableEncodedPasswords() {
    return BooleanUtils.toBooleanDefaultIfNull(getEnableEncodedPasswords(), false);
  }

  public String getEncodedPasswordKeys() {
    return encodedPasswordKeys;
  }

  /**
   * A comma separated list of keys that will be decoded.
   *
   * @param encodedPasswordKey the keys to decode.
   * @see #setEnableEncodedPasswords(Boolean)
   */
  public void setEncodedPasswordKeys(String encodedPasswordKey) {
    encodedPasswordKeys = encodedPasswordKey;
  }

  public ExtraFactoryConfiguration getExtraFactoryConfiguration() {
    return extraFactoryConfiguration;
  }

  /**
   * Configure any additional settings that need to be applied to the {@link javax.jms.ConnectionFactory} after it has been read
   * from the JNDI store.
   * <p>
   * Generally speaking, this is not encouraged, as you are now keeping configuration in 2 separate locations (both JNDI and adapter
   * config). The ConnectionFactory should ideally be configured in JNDI with all the settings that are required for each
   * connection.
   * </p>
   *
   *
   * @param efc any extra configuration, default is {@link NoOpFactoryConfiguration}
   * @see NoOpFactoryConfiguration
   * @see SimpleFactoryConfiguration
   */
  public void setExtraFactoryConfiguration(ExtraFactoryConfiguration efc) {
    extraFactoryConfiguration = Args.notNull(efc, "extra factory configuration");
  }

  public boolean newContextOnException() {
    return BooleanUtils.toBooleanDefaultIfNull(getNewContextOnException(), false);
  }

  public Boolean getNewContextOnException() {
    return newContextOnException;
  }

  /**
   * Whether or not to create a new JNDI context on exception.
   * <p>
   * In some instances, the {@link javax.naming.spi.InitialContextFactory} implementation may be badly behaved, and not
   * re-initialise nicely when error situations occur. By setting this to true, we create a new {@link InitialContext} when any
   * exception is encountered. This will of course have a performance hit if you are constantly resetting the connection for things
   * like a missing JNDI entry; but might help with rare events like recovery after a network outage.
   * </p>
   *
   * @param b true or false, default false.
   */
  public void setNewContextOnException(Boolean b) {
    newContextOnException = b;
  }

}