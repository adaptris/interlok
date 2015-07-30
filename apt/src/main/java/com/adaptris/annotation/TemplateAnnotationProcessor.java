package com.adaptris.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.annotation.processing.ProcessingEnvironment;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

public abstract class TemplateAnnotationProcessor extends AnnotationProcessorImpl {

  protected transient VelocityEngine engine;
  protected transient Template template;

  @Override
  public void init(ProcessingEnvironment env) {
    super.init(env);
    configureVelocityEngine();
  }

  protected String getVelocityTemplate() {
    String name = getDefaultTemplate();
    if (processingEnv.getOptions().containsKey(getOptionTemplateKey())) {
      name = processingEnv.getOptions().get(getOptionTemplateKey());
    }
    return name;
  }

  protected void configureVelocityEngine() {
    InputStream in = null;
    try {
      // Initialise Velocity engine
      Properties props = new Properties();
      URL url = this.getClass().getClassLoader().getResource("velocity.properties");
      in = url.openStream();
      props.load(in);
      engine = new VelocityEngine(props);
      engine.init();
      template = engine.getTemplate(getVelocityTemplate());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      closeQuietly(in);
    }
  }

  protected abstract String getDefaultTemplate();

  protected abstract String getOptionTemplateKey();

}
