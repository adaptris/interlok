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
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XStream version of {@link com.adaptris.core.AdaptrisMarshaller} that supports additional config pre-processors when
 * unmarshalling.
 * 
 * @config xstream-marshaller-with-pre-processing
 * 
 */
@XStreamAlias("xstream-marshaller-with-pre-processing")
@DisplayOrder(order = {"preProcessors", "preProcessorConfig", "preProcessorLoader"})
public class PreProcessingXStreamMarshaller extends com.adaptris.core.XStreamMarshaller {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String preProcessors;
  @NotNull
  @AutoPopulated
  private KeyValuePairSet preProcessorConfig;

  @AdvancedConfig
  private ConfigPreProcessorLoader preProcessorLoader;

  private transient ConfigPreProcessors processors = null;


  public PreProcessingXStreamMarshaller() {
    super();
    setPreProcessorConfig(new KeyValuePairSet());
  }

  @Override
  public Object unmarshal(Reader in) throws CoreException {
    Args.notNull(in, "reader");
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
    Args.notNull(input, "input");
    Object result = null;
    try {
      result = getInstance().fromXML(preProcess(input));
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public Object unmarshal(File file) throws CoreException {
    Args.notNull(file, "file");
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
    Args.notNull(url, "url");
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
    Args.notNull(url, "url");
    Object result = null;
    try (InputStream in = connectToUrl(url)) {
      if (in != null) {
        result = this.unmarshal(in);
      }
      else {
        throw new IOException("could not unmarshal component from [" + url + "]");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  @Override
  public Object unmarshal(InputStream in) throws CoreException {
    Args.notNull(in, "inputstream");
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

  public ConfigPreProcessorLoader getPreProcessorLoader() {
    return preProcessorLoader;
  }

  public void setPreProcessorLoader(ConfigPreProcessorLoader loader) {
    this.preProcessorLoader = loader;
  }

  ConfigPreProcessorLoader preProcessorLoader() {
    return (getPreProcessorLoader() != null) ? getPreProcessorLoader() : new DefaultPreProcessorLoader();
  }

  public String getPreProcessors() {
    return preProcessors;
  }

  public void setPreProcessors(String preProcessorList) {
    this.preProcessors = preProcessorList;
  }

  public KeyValuePairSet getPreProcessorConfig() {
    return preProcessorConfig;
  }

  public void setPreProcessorConfig(KeyValuePairSet preProcessorConfig) {
    this.preProcessorConfig = Args.notNull(preProcessorConfig, "pre-processor config");
  }

  private String preProcess(String rawXML) throws CoreException {
    if (processors == null) {
      processors = preProcessorLoader().load(getPreProcessors(), getPreProcessorConfig());
    }
    return processors.process(rawXML);
  }

}
