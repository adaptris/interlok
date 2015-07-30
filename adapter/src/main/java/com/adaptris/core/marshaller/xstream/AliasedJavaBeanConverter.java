package com.adaptris.core.marshaller.xstream;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.BeanProvider;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanProvider;
import com.thoughtworks.xstream.converters.reflection.MissingFieldException;
import com.thoughtworks.xstream.core.util.FastField;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * XStream converter class for our registered JavaBean classes defined in javabeans.properties.
 * <p>
 * This class Provides the same functionality as {@link AliasedElementReflectionConverter} but for javabeans.
 * </p>
 * 
 * @author bklair
 */
public class AliasedJavaBeanConverter extends JavaBeanConverter {

  protected transient Logger log = LoggerFactory.getLogger(AliasedJavaBeanConverter.class);
  
  
  public AliasedJavaBeanConverter(Mapper mapper) {
    super(mapper);
  }
   
  public AliasedJavaBeanConverter(Mapper mapper, Class type) {
      super(mapper, new BeanProvider(), type);
  }

  /**
   * Marshal the given class to XML representation
   */
  @Override
  public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
    final String classAttributeName = mapper.aliasForSystemAttribute("class");
    
    beanProvider.visitSerializableProperties(source,
        new JavaBeanProvider.Visitor() {
          public boolean shouldVisit(String name, Class definedIn) {
            return mapper.shouldSerializeMember(definedIn, name);
          }

          public void visit(String propertyName, Class fieldType, Class definedIn, Object newObj) {
            if (newObj != null) {
              writeField(propertyName, fieldType, newObj, definedIn);
            }
          }

//          private void writeFieldOld(String propertyName, Class fieldType, Object newObj, Class definedIn) {
//            Class actualType = newObj.getClass();
//            Class defaultType = mapper.defaultImplementationOf(fieldType);
//            String serializedMember = mapper.serializedMember(
//                source.getClass(), propertyName);
//
//            ExtendedHierarchicalStreamWriterHelper.startNode(writer, serializedMember, actualType);
//            if (!actualType.equals(defaultType) && classAttributeName != null) {
//              writer.addAttribute(classAttributeName, mapper.serializedClass(actualType));
//            }
//            context.convertAnother(newObj);
//
//            writer.endNode();
//          }

          // Modified version of method from that super class
          private void writeField(String propertyName, Class fieldType, Object newObj, Class definedIn) {
            Class actualType = newObj.getClass();
            Class defaultType = mapper.defaultImplementationOf(fieldType);
            String serializedMember = mapper.serializedMember(source.getClass(), propertyName);

            // Now where the super class would have written out the field name
            // followed by a class attribute that contained the subclass name,
            // we will write out the subclass alias name as the element with no
            // class attribute at all.
            boolean writeOutSubclassAsElementInsteadOfAttribute = false;
            if (!actualType.equals(defaultType) && classAttributeName != null) {
              writeOutSubclassAsElementInsteadOfAttribute = true;
            }

            if (writeOutSubclassAsElementInsteadOfAttribute) {
              String serializedClassName = mapper.serializedClass(actualType); // Get the alias value of the subclass
              ExtendedHierarchicalStreamWriterHelper.startNode(writer, serializedClassName, actualType);
            }
            else {
              ExtendedHierarchicalStreamWriterHelper.startNode(writer, serializedMember, actualType);
            }
            context.convertAnother(newObj);

            writer.endNode();
          }
        });
  }

  /**
   * Unmarshall the XML data from the reader and create the object instance.
   */
  @Override
  public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
    final Object result = instantiateNewInstance(context);
    final Set<FastField> seenProperties = new HashSet() {
        public boolean add(Object e) {
            if (!super.add(e)) {
                throw new DuplicatePropertyException(((FastField)e).getName());
            }
            return true;
        }
    };

    Class<?> parentClass = result.getClass();
    Class<?> fieldClass = null;
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      boolean propertyProcessed = false;

      String currentNodeName = reader.getNodeName();
      String propertyName = mapper.realMember(parentClass, currentNodeName);

      log.trace("Unmarshalling xml element: {}, processing as property name: {} for class: {}", currentNodeName, propertyName, parentClass.getSimpleName());
      if (mapper.shouldSerializeMember(parentClass, propertyName)) {
        // Is this a valid property for the current class
        boolean propertyExistsInClass = false;
        try {
          propertyExistsInClass = beanProvider.propertyDefinedInClass(propertyName, parentClass);
        } catch (MissingFieldException mfe) {
          propertyExistsInClass = false;
        }

        // Handle the normal case where we have a valid property and process it's value
        if (propertyExistsInClass) {
          Class<?> type = determineType(reader, result, propertyName);
          Object value = context.convertAnother(result, type);
          beanProvider.writeProperty(result, propertyName, value);
          seenProperties.add(new FastField(parentClass, propertyName));
          propertyProcessed = true;
        }
      }
      
      // If the current element has not been matched then it might be of the new format: where the subclass is used as the element node instead of the field name
      if (!propertyProcessed) {
        log.trace("Property: {} not valid for class: {}, attempting resolving property name", propertyName, parentClass.getSimpleName());

        // Try to resolve the current node as an aliases field
        try {
//          String fieldName = mapper.realMember(parentClass, currentNodeName);
          fieldClass = mapper.realClass(propertyName);
        } catch (CannotResolveClassException e) {
          // Try to resolve the current node as an aliased class instead
          fieldClass = mapper.realClass(currentNodeName);
        }

        Field matchedField = XStreamUtils.getMatchedFieldFromClass(parentClass, fieldClass, seenProperties);
        if (matchedField != null) {
          String fieldCapturedName = matchedField.getName();
          Class<?> type = fieldClass;
           Object value = context.convertAnother(result, type);
//          Object value = beanProvider.newInstance(type);
          log.trace("Setting field: {} to class: {}", fieldCapturedName, value.getClass());
          beanProvider.writeProperty(result, fieldCapturedName, value);
          seenProperties.add(new FastField(parentClass, fieldCapturedName));
        }
        // OK we've tried all we can, clearly this must be an invalid field in the xml - abort
        else {
          throw new MissingFieldException(parentClass.getName(), propertyName);
        }
      }
      reader.moveUp();
    }
    return result;
  }

  private Object instantiateNewInstance(UnmarshallingContext context) {
    Object result = context.currentObject();
    if (result == null) {
      result = beanProvider.newInstance(context.getRequiredType());
    }
    return result;
  }

  private Class determineType(HierarchicalStreamReader reader, Object result, String fieldName) {
    final String classAttributeName = mapper.aliasForSystemAttribute("class");
    String classAttribute = classAttributeName == null ? null : reader.getAttribute(classAttributeName);
    if (classAttribute != null) {
      return mapper.realClass(classAttribute);
    }
    else {
      return mapper.defaultImplementationOf(beanProvider.getPropertyType(result, fieldName));
    }
  }
  
}
