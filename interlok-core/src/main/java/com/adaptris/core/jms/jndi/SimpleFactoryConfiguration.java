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

package com.adaptris.core.jms.jndi;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnectionFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.jms.JmsUtils;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExtraFactoryConfiguration} implementation using reflection to configure fields on the ConnectionFactory.
 * 
 * <p>
 * This implementation uses reflection to configure fields on the ConnectionFactory after it has been returned from the JNDI store.
 * Generally speaking, this is not encouraged, as you are now keeping configuration in 2 separate locations (both JNDI and adapter
 * config). The ConnectionFactory should be configured in JNDI with all the settings that are required for each connection.
 * </p>
 * 
 * <p>
 * As the name suggests, this is a very simple implementation, primitive values are supported along with strings, but not objects.
 * Every fieldname referenced is expected to have an associated method set[fieldname] which has a single parameter; the match for
 * which is case-insensitive. If you have more more complex requirements then you will have to write your own implementation of
 * {@link ExtraFactoryConfiguration}.
 * </p>
 * 
 * 
 * @config simple-jndi-factory-configuration
 * @author lchan
 * 
 */
@XStreamAlias("simple-jndi-factory-configuration")
public class SimpleFactoryConfiguration implements ExtraFactoryConfiguration {

  private enum Converter {
    INTEGER_VALUE {
      @Override
      Object convert(String s) throws Exception {
        return Integer.valueOf(s);
      }
    },
    BOOLEAN_VALUE {
      @Override
      Object convert(String s) throws Exception {
        return Boolean.valueOf(s);
      }
    },
    STRING_VALUE {
      @Override
      Object convert(String s) throws Exception {
        return s;
      }
    },
    FLOAT_VALUE {
      @Override
      Object convert(String s) throws Exception {
        return Float.valueOf(s);
      }
    },
    DOUBLE_VALUE {
      @Override
      Object convert(String s) throws Exception {
        return Double.valueOf(s);
      }
    },
    LONG_VALUE {
      @Override
      Object convert(String s) throws Exception {
        return Long.valueOf(s);
      }
    };
    abstract Object convert(String s) throws Exception;
  }

  private static final Class<?>[] PRIMITIVE_ARRAY = {
          int.class, Integer.class, boolean.class, Boolean.class, String.class, float.class, Float.class, double.class,
          Double.class, long.class, Long.class};

  private static final Converter[] CONVERTER_ARRAY = {Converter.INTEGER_VALUE, Converter.INTEGER_VALUE, Converter.BOOLEAN_VALUE,
      Converter.BOOLEAN_VALUE, Converter.STRING_VALUE, Converter.FLOAT_VALUE, Converter.FLOAT_VALUE, Converter.DOUBLE_VALUE,
      Converter.DOUBLE_VALUE, Converter.LONG_VALUE, Converter.LONG_VALUE};

  private static final List<Class<?>> PRIMITIVES = Arrays.asList(PRIMITIVE_ARRAY);
  private static final Map<Class<?>, Converter> PRIMITIVE_CONVERTERS;

  static {
    HashMap<Class<?>, Converter> map = new HashMap<Class<?>, Converter>();
    for (int i = 0; i < PRIMITIVE_ARRAY.length; i++) {
      map.put(PRIMITIVE_ARRAY[i], CONVERTER_ARRAY[i]);
    }
    PRIMITIVE_CONVERTERS = Collections.unmodifiableMap(map);
  }

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet properties;

  public SimpleFactoryConfiguration() {
    setProperties(new KeyValuePairSet());
  }

  @Override
  public void applyConfiguration(Object cf) throws JMSException {
    if((cf instanceof ConnectionFactory) || (cf instanceof XAConnectionFactory))
      applyConfig(cf);
    else
      throw new JMSException("Object to apply configuration is not a XA/ConnectionFactory.");
  }

  private void applyConfig(Object cf) throws JMSException {
    try {
      for (KeyValuePair kvp : getProperties()) {
        invoke(cf, kvp.getKey(), kvp.getValue());
      }
    }
    catch (Exception e) {
      JmsUtils.rethrowJMSException(e);
    }
  }

  public KeyValuePairSet getProperties() {
    return properties;
  }

  /**
   * Set any extra properties that need to be configured on the connection factory.
   * <p>
   * The key portion of the underlying {@link KeyValuePair} should match the name of the underlying ConnectionFactory field, the
   * value is the parameter to the associated setter. Note that only primitive types are supported (long, string, boolean, int,
   * float, double).
   * </p>
   * <p>
   * If, for instance, you are looking up a Aurea SonicMQ connection factory from JNDI, and you wished to change the ConnectID
   * field, then the following configuration would be appropriate.
   * 
   * <pre>
   * {@code 
   *   <properties>
   *     <key-value-pair>
   *        <key>ConnectID</key>
   *        <value>MyConnectId</value>
   *     </key-value-pair>
   *   </properties>
   * }
   * </pre>
   * which will invoke the associated setConnectID(String), setting the ConnectID property to 'MyConnectId'.
   * </p>
   * *
   * 
   * @param extras
   */
  public void setProperties(KeyValuePairSet extras) {
    properties = extras;
  }

  private void invoke(Object obj, String fieldname, String value) throws Exception {
    Method m = getSetter(obj.getClass(), fieldname);
    if (!validate(m, fieldname)) {
      return;
    }
    Class<?> clazz = m.getParameterTypes()[0];
    Object param = PRIMITIVE_CONVERTERS.get(clazz).convert(value);
    m.invoke(obj, new Object[]
    {
      param
    });
  }

  private boolean validate(Method m, String fieldname) {
    if (m == null) {
      log.warn("No method matching set" + fieldname + " found, or method signature mismatch");
      return false;
    }
    if (!PRIMITIVES.contains(m.getParameterTypes()[0])) {
      log.warn("Ignoring " + fieldname + " as the parameter is not considered primitive");
      return false;
    }
    return true;
  }

  private Method getSetter(Class<?> c, String fieldName) {
    Method result = null;
    Method[] methods = c.getMethods();
    for (Method m : methods) {
      String name = m.getName();
      if (name.equalsIgnoreCase("set" + fieldName) && m.getParameterTypes().length == 1) {
        result = m;
        break;
      }
    }
    return result;
  }

}
