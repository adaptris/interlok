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
