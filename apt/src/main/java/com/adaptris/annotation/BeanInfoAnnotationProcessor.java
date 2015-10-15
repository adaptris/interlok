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

import static com.adaptris.annotation.AnnotationConstants.BEAN_INFO_PROPERTIES_FILE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.apache.velocity.VelocityContext;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("com.adaptris.annotation.GenerateBeanInfo")
@SupportedOptions(value =
{
    "beanInfoDebug", "beanInfoVelocityTemplate", "beanInfoMappingFile"
})
public class BeanInfoAnnotationProcessor extends TemplateAnnotationProcessor {

  private static final String OPTION_VERBOSE = "beanInfoDebug";
  private static final String OPTION_VELOCITY_TEMPLATE = "beanInfoVelocityTemplate";
  private static final String OPTION_BEANLIST_FILE = "beanInfoMappingFile";

  private static final String DEFAULT_VELOCITY_TEMPLATE = "beanInfo.vm";

  private Set<String> beanNames = new HashSet<>();
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {

      if (verbose) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Adding classes to " + outputFile);
      }
      
      // Loop around elements
      for (Element elem : roundEnv.getElementsAnnotatedWith(GenerateBeanInfo.class)) {
        // Only process class level elements
        if (elem.getKind().equals(ElementKind.CLASS)) {
          // Create XYZBeanInfo.java for each element
          createBeanInfo((TypeElement) elem);
          // Add class name to XstreamJavabeans.properties file
          beanNames.add(((TypeElement) elem).getQualifiedName().toString());
        }
      }
      
      // If we are done processing, write the file
      if(roundEnv.processingOver()) {
        appendExistingClasses(beanNames);
        FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", outputFile);
        try(PrintWriter w = new PrintWriter(fo.openWriter())) {
          for (String name : beanNames) {
            w.println(name);
          }
        }
        beanNames.clear();
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  private void appendExistingClasses(Set<String> names) throws IOException {
    // Read the existing file (if any) and put all those names in the set
    FileObject fo = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", outputFile);
    try (BufferedReader r = new BufferedReader(fo.openReader(false))) {
      String className;
      while ((className = r.readLine()) != null) {
        names.add(className);
      }
    }
    catch (FileNotFoundException e) {
      // Ignore it. The file doesn't exist yet, which it fine.
    }
  }

  private void createBeanInfo(TypeElement classElement) throws Exception {
    if (verbose) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating BeanInfo for " + classElement.getSimpleName());
    }

    String className = classElement.getSimpleName().toString();
    String qualifiedName = classElement.getQualifiedName().toString();
    PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
    String packageName = packageElement.getQualifiedName().toString();

    Map<String, VariableElement> fields = new HashMap<String, VariableElement>();

    Writer writer = null;

    try {
      // Build Bean Info
      TypeElement curClassElement = classElement;
      do {

        for (Element e : curClassElement.getEnclosedElements()) {
          if (e.getKind().equals(ElementKind.FIELD) && !e.getModifiers().contains(Modifier.TRANSIENT)
              && !e.getModifiers().contains(Modifier.STATIC)) {

            fields.put(e.getSimpleName().toString(), (VariableElement) e);
          }
        }

        TypeMirror t = curClassElement.getSuperclass();
        if (t instanceof DeclaredType) {
          curClassElement = (TypeElement) ((DeclaredType) t).asElement();
        }
        else {
          curClassElement = null;
        }

      }
      while (curClassElement != null);
      VelocityContext vc = new VelocityContext();
      vc.put("className", className);
      vc.put("packageName", packageName);
      vc.put("fields", fields);
      // Create source file
      JavaFileObject jfo = processingEnv.getFiler().createSourceFile(qualifiedName + "BeanInfo");
      if (verbose) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "creating source file: " + jfo.toUri());
      }
      writer = jfo.openWriter();
      template.merge(vc, writer);
    }
    finally {
      closeQuietly(writer);
    }
  }

  @Override
  protected String getDefaultTemplate() {
    return DEFAULT_VELOCITY_TEMPLATE;
  }

  @Override
  protected String getOptionTemplateKey() {
    return OPTION_VELOCITY_TEMPLATE;
  }

  @Override
  protected String getOptionVerboseKey() {
    return OPTION_VERBOSE;
  }

  @Override
  protected String getOutputFileOverride() {
    return OPTION_BEANLIST_FILE;
  }

  @Override
  protected String getDefaultOutputFile() {
    return BEAN_INFO_PROPERTIES_FILE;
  }

}
