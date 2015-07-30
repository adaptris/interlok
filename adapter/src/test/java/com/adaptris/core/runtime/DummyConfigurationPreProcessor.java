package com.adaptris.core.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.util.ExceptionHelper;

public class DummyConfigurationPreProcessor extends AbstractConfigurationPreProcessor {

  public DummyConfigurationPreProcessor(BootstrapProperties properties) {
    super(properties);
  }

  @Override
  public String process(String xml) throws CoreException {
    return xml;
  }

  @Override
  public String process(URL urlToXml) throws CoreException {
    String xml = null;
    try (InputStream in = urlToXml.openStream()) {
      xml = IOUtils.toString(in);
    }
    catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return xml;
  }

}
