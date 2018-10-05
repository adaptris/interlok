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

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;

abstract class AnnotationProcessorImpl extends AbstractProcessor {

  protected transient String outputFile = null;
  protected transient boolean verbose = false;

  @Override
  public void init(ProcessingEnvironment env) {
    super.init(env);
    configureOutputFile();
    configureVerbosity();
  }

  private void configureVerbosity() {
		if (processingEnv.getOptions().containsKey(getOptionVerboseKey())) {
			verbose = Boolean.valueOf(processingEnv.getOptions().get(getOptionVerboseKey()));
		}
	}

	protected void closeQuietly(Closeable w) {
		try {
			if (w != null) {
        w.close();
      }
		}
	    catch (IOException e) {}
	}

  private void configureOutputFile() {
    if (processingEnv.getOptions().containsKey(getOutputFileOverride())) {
      outputFile = processingEnv.getOptions().get(getOutputFileOverride());
    }
    else {
      outputFile = getDefaultOutputFile();
    }
  }

  protected static boolean isEmpty(String s) {
    return s == null || "".equals(s);
  }

  protected abstract String getOptionVerboseKey();

  protected abstract String getOutputFileOverride();

  protected abstract String getDefaultOutputFile();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

}
