package com.adaptris.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XStream version of {@link AdaptrisMarshaller} that supports additional config pre-processors when unmarshalling.
 * 
 * @config xstream-marshaller-with-pre-processing
 * 
 */
@XStreamAlias("xstream-marshaller-with-pre-processing")
public class PreProcessingXStreamMarshaller extends com.adaptris.core.XStreamMarshaller {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  @AdvancedConfig
  @NotNull
  @AutoPopulated
  private ConfigPreProcessorLoader preprocessorLoader = new DefaultPreProcessorLoader();

  private String preProcessorList;
  @NotNull
  @AutoPopulated
  private KeyValuePairSet preProcessorConfig;

  private transient ConfigPreProcessors preProcessors = null;


  public PreProcessingXStreamMarshaller() {
    super();
  }

  @Override
  public Object unmarshal(Reader in) throws CoreException {
    Args.notNull(in, "Attempt to unmarshal null reader");
    Object result = null;
    try {
      String xml = IOUtils.toString(in);
      result = unmarshal(xml);
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }

  @Override
  public Object unmarshal(String input) throws CoreException {
    Args.notNull(input, "Attempt to unmarshal null string");
    return getInstance().fromXML(preProcess(input));
  }

  @Override
  public Object unmarshal(File file) throws CoreException {
    Args.notNull(file, "Attempt to unmarshal null file");
    Object result = null;
    try  {
      result = unmarshal(new FileInputStream(file));
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public Object unmarshal(URL url) throws CoreException {
    Args.notNull(url, "Attempt to unmarshal null url");
    Object result = null;
    try {
      result = this.unmarshal(url.openStream());
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public Object unmarshal(URLString url) throws CoreException {
    Args.notNull(url, "Attempt to unmarshal null url");
    Object result = null;
    try (InputStream in = connectToUrl(url)) {
      if (in != null) {
        result = this.unmarshal(in);
      }
      else {
        throw new CoreException("could not unmarshal component from [" + url + "]");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public Object unmarshal(InputStream in) throws CoreException {
    Args.notNull(in, "Attempt to unmarshal null inputstream");
    Object result = null;
    try {
      String xml = IOUtils.toString(in);
      result = unmarshal(xml);
    }
    catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return result;
  }

  public ConfigPreProcessorLoader getPreprocessorLoader() {
    return preprocessorLoader;
  }

  public void setPreprocessorLoader(ConfigPreProcessorLoader loader) {
    this.preprocessorLoader = loader;
  }

  public String getPreProcessorList() {
    return preProcessorList;
  }

  public void setPreProcessorList(String preProcessorList) {
    this.preProcessorList = preProcessorList;
  }

  public KeyValuePairSet getPreProcessorConfig() {
    return preProcessorConfig;
  }

  public void setPreProcessorConfig(KeyValuePairSet preProcessorConfig) {
    this.preProcessorConfig = Args.notNull(preProcessorConfig, "pre-processor config");
  }

  private String preProcess(String rawXML) throws CoreException {
    if (preProcessors == null) {
      preProcessors = getPreprocessorLoader().load(getPreProcessorList(), getPreProcessorConfig());
    }
    return preProcessors.process(rawXML);
  }

}
