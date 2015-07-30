package com.adaptris.core.runtime;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.util.ExceptionHelper;

public class ConfigurationPreProcessorFactory implements ConfigurationPreProcessorLoader {

  private transient Logger log = LoggerFactory.getLogger(ConfigurationPreProcessorFactory.class);

  private static final String PRE_PROCESSOR_SEPARATOR = ":";

  private static final String NAME_PROPERTY_KEY = "name";
  private static final String CLASS_PROPERTY_KEY = "class";

  private static final transient String PRE_PROCESSOR_RESOURCE = "META-INF/com/adaptris/core/preprocessor/";

  private ExternalPreProcessorPropertyLoader externalPreProcessorPropertyLoader;

  public ConfigurationPreProcessorFactory() {
    this.setExternalPreProcessorPropertyLoader(new ExternalPreProcessorPropertyLoader());
  }

  @Override
  public PreProcessorsList load(BootstrapProperties bootstrapProperties) throws CoreException {
    PreProcessorsList preProcessorsList = new PreProcessorsList();
    String prePrecessorList = BootstrapProperties.getPropertyIgnoringCase(bootstrapProperties,
        AdapterConfigManager.CONFIGURATION_PRE_PROCESSORS);
    if (!isEmpty(prePrecessorList)) {
      String[] configuredPreProcessors = prePrecessorList.split(PRE_PROCESSOR_SEPARATOR);
      for (String preProcessor : configuredPreProcessors) {
        try {
          preProcessorsList.add(resolve(preProcessor, bootstrapProperties));
        }
        catch (Exception e) {
          log.warn("Unable to find pre-processor with name: ]{}].  Ignoring.", preProcessor);
        }
      }
    }
    return preProcessorsList;
  }

  private ConfigurationPreProcessor resolve(String name, BootstrapProperties bootstrapProperties) throws CoreException {
    ConfigurationPreProcessor result = null;
    Properties p = getExternalPreProcessorPropertyLoader().loadPropertyFile(name);
    String classname = p.getProperty(CLASS_PROPERTY_KEY);
    if (isBlank(classname)) {
      result = this.createInstance(name, bootstrapProperties);
    }
    else {
      result = this.createInstance(classname, bootstrapProperties);
    }
    return result;
  }

  private ConfigurationPreProcessor createInstance(String classname, BootstrapProperties bootstrapProperties) throws CoreException {
    ConfigurationPreProcessor preProcessor = null;

    log.debug("Loading pre-processor: " + classname);
    Class<?>[] paramTypes =
    {
      BootstrapProperties.class
    };
    Object[] args =
    {
      bootstrapProperties
    };

    Class<?> clazz;
    try {
      clazz = Class.forName(classname);
      Constructor<?> cnst = clazz.getDeclaredConstructor(paramTypes);
      preProcessor = ((ConfigurationPreProcessor) cnst.newInstance(args));
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }

    return preProcessor;
  }

  public ExternalPreProcessorPropertyLoader getExternalPreProcessorPropertyLoader() {
    return externalPreProcessorPropertyLoader;
  }

  public void setExternalPreProcessorPropertyLoader(ExternalPreProcessorPropertyLoader externalPreProcessorPropertyLoader) {
    this.externalPreProcessorPropertyLoader = externalPreProcessorPropertyLoader;
  }

  class ExternalPreProcessorPropertyLoader {

    Properties loadPropertyFile(String name) {
      Properties p = new Properties();
      try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(PRE_PROCESSOR_RESOURCE + name)) {
        if (in != null) {
          p.load(in);
        }
      }
      catch (IOException e) {
        // Just return an empty properties is fine.
      }
      return p;
    }
  }

}
