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
import static com.adaptris.annotation.AnnotationConstants.CDATA_PROPERTIES_FILE;
import static com.adaptris.annotation.AnnotationConstants.STANDARD_FIELD_SEPARATOR;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("com.adaptris.annotation.MarshallingCDATA")
@SupportedOptions(value =
{
    "cdataMappingFile", "cdataDebug"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class CDataAnnotationProcessor extends AnnotationProcessorImpl {

	private static final String OPTION_CDATA_MAPPING_FILE = "cdataMappingFile";
	private static final String OPTION_CDATA_VERBOSE = "cdataDebug";

	private Set<String> entries = new HashSet<>();
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
      if (verbose) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Adding classes to " + outputFile);
      }

			for (Element elem : roundEnv.getElementsAnnotatedWith(MarshallingCDATA.class)) {
				if (elem.getKind().equals(ElementKind.FIELD)) {
					Name fqn = ((VariableElement) elem).getSimpleName();
					Element parentClass = ((VariableElement) elem).getEnclosingElement();
          entries.add(parentClass.toString() + STANDARD_FIELD_SEPARATOR + fqn);
					if (verbose) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Added " + fqn);
					}
				}
			}
			
      // If we are done processing, write the file
      if(roundEnv.processingOver()) {
        appendExistingEntries(entries);
        
        FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", outputFile);
        try(PrintWriter w = new PrintWriter(fo.openWriter())) {
          for(String entry: entries) {
            w.println(entry);
          }
        }
        entries.clear();
      }
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return true;
	}
	
  private void appendExistingEntries(Set<String> entries) throws IOException {
    // Read the existing file (if any) and put all those names in the set
    FileObject fo = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", outputFile);
    try(BufferedReader r = new BufferedReader(fo.openReader(false))) {
      String className;
      while((className = r.readLine()) != null) {
        entries.add(className);
      }
    } catch(FileNotFoundException e) {
      // Ignore it. The file doesn't exist yet, which it fine.
    }
  }

  @Override
  protected String getOptionVerboseKey() {
    return OPTION_CDATA_VERBOSE;
  }

  @Override
  protected String getOutputFileOverride() {
    return OPTION_CDATA_MAPPING_FILE;
  }

  @Override
  protected String getDefaultOutputFile() {
    return CDATA_PROPERTIES_FILE;
  }
}
