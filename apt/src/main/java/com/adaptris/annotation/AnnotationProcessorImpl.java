package com.adaptris.annotation;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;

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

}
