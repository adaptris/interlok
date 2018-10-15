package com.adaptris.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

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
import com.adaptris.util.URLHelper;
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
  public Object unmarshal(Reader input) throws CoreException {
    Args.notNull(input, "reader");
    try (Reader in = input) {
      String xml = IOUtils.toString(in);
      return unmarshal(xml);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public Object unmarshal(String input) throws CoreException {
    Args.notNull(input, "input");
    Object result = null;
    try {
      result = getInstance().fromXML(preProcess(input)); // lgtm [java/unsafe-deserialization]
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    return result;
  }

  @Override
  public Object unmarshal(File file) throws CoreException {
    Args.notNull(file, "file");
    try (FileInputStream in = new FileInputStream(file)) {
      return unmarshal(in);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public Object unmarshal(URL url) throws CoreException {
    Args.notNull(url, "url");
    try (InputStream in = url.openStream()) {
      return this.unmarshal(in);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public Object unmarshal(URLString url) throws CoreException {
    Args.notNull(url, "url");
    try (InputStream in = URLHelper.connect(url)) {
      return this.unmarshal(in);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public Object unmarshal(InputStream input) throws CoreException {
    Args.notNull(input, "inputstream");
    try (InputStream in = input) {
      String xml = IOUtils.toString(in, Charset.defaultCharset());
      return unmarshal(xml);
    }
    catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
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
