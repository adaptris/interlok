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
import static com.adaptris.annotation.AnnotationConstants.XSTREAM_ALIAS_PROPERTIES_FILE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
*/

@SupportedAnnotationTypes("com.thoughtworks.xstream.annotations.XStreamAlias")
@SupportedOptions(value =
{
    "xstreamMappingFile", "xstreamDebug"
})
public class XStreamAnnotationProcessor extends AnnotationProcessorImpl {

  private static final String OPTION_XSTREAM_MAPPING_FILE = "xstreamMappingFile";
  private static final String OPTION_XSTREAM_VERBOSE = "xstreamDebug";

  private final Set<String> classNames = new HashSet<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      if (verbose && !roundEnv.processingOver()) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Adding classes to " + outputFile);
      }

      for (Element elem : roundEnv.getElementsAnnotatedWith(XStreamAlias.class)) {
        // XStreamAlias directive = elem.getAnnotation(XStreamAlias.class);
        if (elem.getKind().equals(ElementKind.CLASS)) {
          Name fqn = ((TypeElement) elem).getQualifiedName();
          classNames.add(fqn.toString());
          if (verbose) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Added " + fqn);
          }
        }
      }
      
      // If we are done processing, write the file. If the file already exists, include the existing contents in the new file.
      if(roundEnv.processingOver()) {
        appendExistingClasses(classNames);
        
        FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", outputFile);
        try(PrintWriter w = new PrintWriter(fo.openWriter())) {
          for(String name: classNames) {
            w.println(name);
          }
        }
        classNames.clear();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    return true; // no further processing of this annotation type
  }
  
  private void appendExistingClasses(Set<String> names) throws IOException {
    // Read the existing file (if any) and put all those names in the set
    FileObject fo = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", outputFile);
    try(BufferedReader r = new BufferedReader(fo.openReader(false))) {
      String className;
      while((className = r.readLine()) != null) {
        names.add(className);
      }
    } catch(FileNotFoundException e) {
      // Ignore it. The file doesn't exist yet, which it fine.
    }
  }

  @Override
  protected String getOptionVerboseKey() {
    return OPTION_XSTREAM_VERBOSE;
  }

  @Override
  protected String getOutputFileOverride() {
    return OPTION_XSTREAM_MAPPING_FILE;
  }

  @Override
  protected String getDefaultOutputFile() {
    return XSTREAM_ALIAS_PROPERTIES_FILE;
  }
}
