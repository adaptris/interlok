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

package com.adaptris.core;

import static com.adaptris.annotation.AnnotationConstants.BEAN_INFO_PROPERTIES_FILE;
import static com.adaptris.annotation.AnnotationConstants.CDATA_PROPERTIES_FILE;
import static com.adaptris.annotation.AnnotationConstants.XSTREAM_ALIAS_PROPERTIES_FILE;
import static com.adaptris.annotation.AnnotationConstants.XSTREAM_IMPLICIT_PROPERTIES_FILE;
import static com.adaptris.core.marshaller.xstream.XStreamUtils.getClasses;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.marshaller.xstream.AliasedElementReflectionConverter;
import com.adaptris.core.marshaller.xstream.AliasedJavaBeanConverter;
import com.adaptris.core.marshaller.xstream.JsonPrettyStaxDriver;
import com.adaptris.core.marshaller.xstream.LowerCaseHyphenatedMapper;
import com.adaptris.core.marshaller.xstream.PrettyStaxDriver;
import com.adaptris.core.marshaller.xstream.XStreamUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * XStream Marshaller Specific Factory class.
 * 
 * @author bklair
 */
public class AdapterXStreamMarshallerFactory extends AdapterMarshallerFactory {

	private transient static Logger log = LoggerFactory.getLogger(AdapterXStreamMarshallerFactory.class);

	public static final transient boolean XSTREAM_DBG = Boolean.getBoolean("adp.xstream.debug");

	public static enum OutputMode {STANDARD, ALIASED_SUBCLASSES};
	
	private transient static Set<String>    cdataFields              = new HashSet<>();
	private transient static Set<String>    xstreamImplicits         = new HashSet<>();
	private transient static List<Class<?>> xstreamAnnotatedClasses  = new ArrayList<>();
	private transient static List<Class<?>> beanInfoAnnotatedClasses = new ArrayList<>();

	// TODO we should have a AdapterMarshallerFactory singleton located somewhere else instead of here
	// But for the time being just use this class as a singleton until a better solution is found
	protected static AdapterXStreamMarshallerFactory instance;
	protected OutputMode mode = OutputMode.STANDARD;

	
  private AdapterXStreamMarshallerFactory() {
		super();
		setMode(OutputMode.STANDARD);
		// Read in and store all the properties associated with xstream
		readXStreamConfigProperties();
	}
	
	// Singleton accessor method
	public static synchronized AdapterXStreamMarshallerFactory getInstance() {
	  if (instance == null) {
	    instance = new AdapterXStreamMarshallerFactory();
	  }
	  return instance;
	}
	
	@Override
	public AdaptrisMarshaller createMarshaller() throws CoreException {
		return createMarshaller(MarshallingOutput.XML);
	}
	
	@Override
	public AdaptrisMarshaller createMarshaller(MarshallingOutput outputType) throws CoreException {
    // TODO The following Marshaller classes are problematic because they are
    // like Singletons and so have a callback to this factory class to complete
    // instantiation, we should ideally fully instantiate them here and now.
		switch(outputType) {
    case JSON:
      return new XStreamJsonMarshaller();
    case XML:
      return new XStreamMarshaller();
    default:
      throw new IllegalArgumentException("Failed to create XStreamMarshaller due to invalid output type: "+outputType);
		}
	}
	
	/**
	 * Reads in and stores all of the xstream configuration items from various resources
	 */
	protected void readXStreamConfigProperties() {
		// Process annotations
		processAnnotations();
		// Process classes with special field handling
		processCdata();
		// Process classes with special hierarchy
		processBeanInfo();
		// Process classes with implicit collection
		processXStreamImplicit();
	}
	
	/**
	 * Creates the XStream object instance
	 * 
	 * @param outputMode - The type of output to generate ie JSON/XML
	 * @return - newly created instance
	 */
	protected XStream createXStreamInstance(MarshallingOutput outputMode) {
		XStream xstream = null;
		
		if (outputMode == MarshallingOutput.XML) {
		  xstream = createXStreamInstance(outputMode, new PureJavaReflectionProvider(), new PrettyStaxDriver(cdataFields));
		}
		else if (outputMode == MarshallingOutput.JSON) {
		  if (getMode() == OutputMode.STANDARD) {
		    xstream = createXStreamInstance(outputMode, new PureJavaReflectionProvider(), new JettisonMappedXmlDriver());
		  }
		  else {
		    xstream = createXStreamInstance(outputMode, new PureJavaReflectionProvider(), new JsonPrettyStaxDriver());
		  }
		}
		else {
			throw new IllegalArgumentException("Marshalling option not supported by XStream Factory: " + outputMode);
		}
		return xstream;
  }

  /**
   * Create XStream instance from the given parameters
   * 
   * @param outputMode the output mode.
   * @param rd the reflection provider
   * @param hsd the stream driver.
   * @return an XStream implementation
   */
  protected XStream createXStreamInstance(MarshallingOutput outputMode, ReflectionProvider rd, HierarchicalStreamDriver hsd) {
    XStream xstream = null;
    if (outputMode == MarshallingOutput.XML) {
      xstream = new MyExtremeMarshaller(rd, hsd);
    }
    else if (outputMode == MarshallingOutput.JSON) {
      xstream = new MyExtremeMarshaller(rd, hsd);
    }
    else {
      throw new IllegalArgumentException("Marshalling option not supported by XStream Factory: " + outputMode);
    }
    return xstream;
  }
	 
	/**
	 * Public method that returns a configure XStream object instance read for use
	 * @return Xstream configured for XML output
	 */
	public XStream createXStream() {
		final MarshallingOutput outputType = MarshallingOutput.XML;
		return createXStream(outputType);
	}
	
	  /**
   * Allows the construction of an XStream Object with the specified parameters for XML output
   * 
   * @param rd - ReflectionProvider
   * @param hsd - HierarchicalStreamDriver
   * @return an XStream implementation
   */
  public XStream createXStream(ReflectionProvider rd, HierarchicalStreamDriver hsd) {
    final MarshallingOutput outputType = MarshallingOutput.XML;
    XStream xStreamInstance = createXStreamInstance(outputType, rd, hsd);
    return configureXStream(xStreamInstance, outputType);
  }
  
	/**
	 * Public method that returns a configure XStream object instance read for use configured for the given form of output.
	 * @param outputType - Type of output to configure XStream for ie JSON or XML
	 * @return Xstream configured for the specified output type
	 */
	public XStream createXStream(MarshallingOutput outputType) {
		XStream xStreamInstance = createXStreamInstance(outputType);
		return configureXStream(xStreamInstance, outputType);
	}
	
	/**
	 * Configures the given XStream object.<br />
	 * It is expected that the Factory has been initialised and that all of the
	 * configuration settings have been processed and stored already.
	 * 
	 * @param xstream - XStream instance to configure
	 * @param outputMode - The output type to configure XStream for
	 */
	@SuppressWarnings("rawtypes")
	protected XStream configureXStream(XStream xstream, MarshallingOutput outputMode) {
    // CVE-2017-7957
    xstream.denyTypes(new Class[]{ void.class, Void.class });   
		// Configure Annotations
		Class[] annotationClassesArray = xstreamAnnotatedClasses.toArray(new Class[0]);
		xstream.processAnnotations(annotationClassesArray);
		
		// Configure Bean info
		if (beanInfoAnnotatedClasses != null && beanInfoAnnotatedClasses.size() > 0) {
			for (Iterator<?> iterator = beanInfoAnnotatedClasses.iterator(); iterator.hasNext();) {
				Class<?> beanClass = (Class<?>) iterator.next();
				JavaBeanConverter converter = null;
				// remineID #2457 Add in subclass alias beautifier if mode has been set.
				if (outputMode == MarshallingOutput.XML && getMode() == OutputMode.ALIASED_SUBCLASSES) {
				  converter = new AliasedJavaBeanConverter(xstream.getMapper(), beanClass);
				}
				else {
				  converter = new JavaBeanConverter(xstream.getMapper(), beanClass);
				}
				xstream.registerConverter(converter);
			}
		}
		
		// Cdata is used by the PrettyStaxDriver
		
		// XstreamImplicits is used by the LowerCaseHyphenatedMapper
		
		// remineID #2457 Add in subclass alias beautifier if mode has been set.
		if (outputMode == MarshallingOutput.XML && getMode() == OutputMode.ALIASED_SUBCLASSES) {
		  xstream.registerConverter(new AliasedElementReflectionConverter(
		      xstream.getMapper(), xstream.getReflectionProvider()),
	        XStream.PRIORITY_VERY_LOW);
		}
		
		// Now set any additional items
		if (outputMode == MarshallingOutput.JSON) {
			xstream.setMode(XStream.NO_REFERENCES);	
		}
		else {
			xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
		}
		
		xstream.addDefaultImplementation(ArrayList.class, Collection.class);
		return xstream;
	}

	/**
	 * Obtains a list of all of the Annotated class from the specified resource file
	 */
	protected void processAnnotations() {
		PropertyListProcessor<Class<?>> annotationsProcessor = new PropertyClassListProcessor();
		annotationsProcessor.process(XSTREAM_ALIAS_PROPERTIES_FILE, xstreamAnnotatedClasses);
	}
	
	/**
	 * Read in the resources that specifies which class fields are to be treated as CDATA rather than plain text
	 */
	protected void processCdata() {
		PropertyListProcessor<String> cdataProcessor = new PropertyStringListProcessor();
		cdataProcessor.process(CDATA_PROPERTIES_FILE, cdataFields);
	}
	
	/**
	 * Read in the resource that details the class collection fields which are
	 * to be treated as implicit ie the collection wrapper name will not be
	 * output.
	 */
	protected void processXStreamImplicit() {
		PropertyListProcessor<String> implicitCollectionProcessor = new PropertyStringListProcessor();
		implicitCollectionProcessor.process(XSTREAM_IMPLICIT_PROPERTIES_FILE, xstreamImplicits);
	}

	/**
	 * Reads in the resource that specifies which classes are to be treated as JavaBeans. 
	 */
	protected void processBeanInfo() {
		PropertyListProcessor<Class<?>> beanInfoProcessor = new PropertyClassListProcessor();
		beanInfoProcessor.process(BEAN_INFO_PROPERTIES_FILE, beanInfoAnnotatedClasses);
	}
	
	public OutputMode getMode() {
    return mode;
  }

  public void setMode(OutputMode mode) {
    this.mode = mode;
  }
  
	// ------------------------------------------------------------------------
	//                           Inner Classes
	// ------------------------------------------------------------------------
	
	/**
	 * Helper class that processes a given list of resources. Each resource is
	 * read and processed. This allows subclasses to focus on processes the
	 * resource file contents in a specific way without the boilerplate.
	 * 
	 * @author bklair
	 * 
	 * @param <T> - Type of data contained by resource file - just text data or classes
	 */
	abstract class PropertyListProcessor<T> {

		public void process(String resourceName, Collection<T> propertyCollection) {
			try {
				Enumeration<URL> mappings = AdapterXStreamMarshallerFactory.class.getClassLoader().getResources(resourceName);
				while (mappings.hasMoreElements()) {
					URL mappingUrl = mappings.nextElement();
					try (InputStream in = mappingUrl.openStream()) {
						processEachUrl(propertyCollection, in);
					} catch(IOException ioe) {
						log.warn("Error encountered reading resource URL: {}, continuing processing", mappings);
					}
				}
			} catch (IOException ignore) {
				log.warn("Error encountered reading resource: {}, continuing processing", resourceName);
			}
		}

		/**
		 * Processes the current resource file and adds the processed contents to the given Collection.
		 * @param propertyCollection - Target collection to populate with the resource contents
		 * @param in - Opened Input Stream to the resource file
		 * @throws IOException - If problem encountered 
		 */
		protected abstract void processEachUrl(Collection<T> propertyCollection, InputStream in) throws IOException;
		
	}
	
	/**
	 * Processes the resources as a List of text data
	 * @author bklair
	 */
	class PropertyStringListProcessor extends PropertyListProcessor<String> {
		
		@Override
		protected void processEachUrl(Collection<String> propertyCollection, InputStream in) throws IOException {
			propertyCollection.addAll(XStreamUtils.readResource(in));
		}
	}

	/**
	 * Processes the resources as a List of defined Classes
	 * @author bklair
	 */
	class PropertyClassListProcessor extends PropertyListProcessor<Class<?>> {
		
		@Override
		protected void processEachUrl(Collection<Class<?>> propertyCollection, InputStream in) throws IOException {
			propertyCollection.addAll(getClasses(in));
		}
	}

	// XStream subclass that adds a custom mapper to handle ImplicitCollection names 
	static class MyExtremeMarshaller extends XStream {

		public MyExtremeMarshaller() {
			super();
		}

		public MyExtremeMarshaller(ReflectionProvider rd, HierarchicalStreamDriver hsd) {
			super(rd, hsd);
		}

		@Override
		protected MapperWrapper wrapMapper(MapperWrapper next) {
			return new LowerCaseHyphenatedMapper(next, xstreamImplicits);
		}

	}
}
