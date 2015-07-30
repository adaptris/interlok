package com.adaptris.core.marshaller.xstream;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

import com.adaptris.core.AdapterXStreamMarshallerFactory;
import com.thoughtworks.xstream.core.util.FastField;

/**
 * General Utilities used by various XStream related classes
 * 
 * @author bklair
 */
public class XStreamUtils {
	
	private static Logger log = LoggerFactory.getLogger(XStreamUtils.class);
	
	/**
	 * Converts a lowercase hyphen separated format into a camelcase based
	 * format. Used by the unmarshalling process to convert an xml element into
	 * a java class/field name.
	 * 
	 * @param xmlElementName
	 *            - Current element name to be processed.
	 * @return translated name
	 */
  public static String toFieldName(String xmlElementName) {
    if (xmlElementName == null) {
      return null;
    }
    if (xmlElementName.length() == 0) {
      return xmlElementName;
    }
    if (xmlElementName.length() == 1) {
      return xmlElementName.toLowerCase();
    }
    // -- Follow the Java beans Introspector::decapitalize
    // -- convention by leaving alone String that start with
    // -- 2 uppercase characters.
    if (Character.isUpperCase(xmlElementName.charAt(0))
        && Character.isUpperCase(xmlElementName.charAt(1))) {
      return xmlElementName;
    }
    // -- process each character
    StringBuilder input = new StringBuilder(xmlElementName);
    StringBuilder output = new StringBuilder();
    output.append(Character.toLowerCase(input.charAt(0)));
    boolean multiHyphens = false;
    for (int i = 1; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (ch == '-') {
        if (input.charAt(++i) != '-') {
          output.append(Character.toUpperCase(input.charAt(i)));
        } else {
          multiHyphens = true;
        }
      } else {
        if (multiHyphens) {
          output.append(Character.toUpperCase(ch));
        } else {
          output.append(ch);
        }
        multiHyphens = false;

      }
    }
    return output.toString();
  }
    
    /**
   * Converts a camelcase name into a lowercase hyphen separated format for output to XML. Used by the marshalling process to
   * convert a java class/field name into an xml element name.
   * 
   * @param fieldName - Current element name to be processed.
   * @return translated name
   */
  public static String toXmlElementName(String fieldName) {
    if (fieldName == null) {
      return null;
    }
    if (fieldName.length() == 0) {
      return fieldName;
    }
    if (fieldName.length() == 1) {
      return fieldName.toLowerCase();
    }

    // -- Follow the Java beans Introspector::decapitalize
    // -- convention by leaving alone String that start with
    // -- 2 uppercase characters.
    if (Character.isUpperCase(fieldName.charAt(0))
        && Character.isUpperCase(fieldName.charAt(1))) {
      return fieldName;
    }

    // -- process each character
    StringBuilder cbuff = new StringBuilder(fieldName);
    cbuff.setCharAt(0, Character.toLowerCase(cbuff.charAt(0)));

    boolean ucPrev = false;
    for (int i = 1; i < cbuff.length(); i++) {
      char ch = cbuff.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (ucPrev) {
          continue;
        }
        ucPrev = true;
        cbuff.insert(i, '-');
        ++i;
        cbuff.setCharAt(i, Character.toLowerCase(ch));
      } else {
        ucPrev = false;
      }
    }
    return cbuff.toString();
  }
	
    /**
	 * Given a Field of a Class this method will return a Set of a number of
	 * possible fully qualified reference names for the field. This would be
	 * based on the class hierarchy eg currentClass:field, parentClass:field,
	 * grandparentClass:field etc.
	 * 
	 * @param clazz - Parent Class of the given field
	 * @param field - Given field to process
	 * @param separator - class-field separator
	 * @return - Set<String> of possible paths for the field
	 */
  public static Collection<String> createParentFields(Class<?> clazz, String field, String separator) {
    Set<String> result = new HashSet<String>();
    result.add(clazz.getCanonicalName() + separator + field);
    Class<?> c = clazz;
    while (c.getSuperclass() != null) {
      c = c.getSuperclass();
      result.add(c.getCanonicalName() + separator + field);
    }
    return result;
  }
	
  /**
   * Determine if the given Set <code>toCheck</code> contains any of the elements within <code>possibles</code>
   * 
   * @param toCheck - Set of Strings to check
   * @param possibles - Collection of
   * @return true if the set contains the possibles.
   */
  public static boolean setContainsAnyOf(Set<String> toCheck, Collection<String> possibles) {
    Set<String> copy = new HashSet<String>(toCheck);
    copy.retainAll(possibles);
    return copy.size() > 0;
  }
    
  /**
   * Reads in the entire file contents skipping any blank lines.
   * 
   * @param in - InputStream to read
   * @return List<String> List of lines.
   * @throws IOException
   */
  public static List<String> readResource(InputStream in) throws IOException {
    List<String> result = new ArrayList<String>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      while (line != null && !isEmpty(line)) {
        result.add(line);
        line = reader.readLine();
      }
    } finally {
      closeQuietly(reader);
    }
    return result;
  }
	
  /**
   * Reads in the given resource file and converts each line of input into a
   * Class.
   * 
   * @param in - Opened input stream to resource file
   * @return List of Classes
   * @throws IOException
   */
  public static List<Class<?>> getClasses(InputStream in) throws IOException {
    List<Class<?>> result = new ArrayList<Class<?>>();
    List<String> lines = readResource(in);
    for (String clazz : lines) {
      try {
        result.add(Class.forName(clazz));
      } catch (ClassNotFoundException e) {
        if (AdapterXStreamMarshallerFactory.XSTREAM_DBG) {
          log.trace("Ignoring missing class [{}] :{}", e.getClass()
              .getSimpleName(), e.getMessage());
        }
      } catch (NoClassDefFoundError e) {
        if (AdapterXStreamMarshallerFactory.XSTREAM_DBG) {
          log.trace("Ignoring missing class [{}] :{}", e.getClass()
              .getSimpleName(), e.getMessage());
        }
      }
    }
    return result;
  }
  
  /**
   * Gets all fields for a class.
   * @param clazz - class to process
   * @param fields - set of fields
   * @return - List of fields
   */
  static List<Field> getFieldsForClassEnsuringUniqueFieldNames(Class<?> clazz, List<Field> fields) {
    // Process the parents members first
    if (clazz.getSuperclass() != null) {
      getFieldsForClassEnsuringUniqueFieldNames(clazz.getSuperclass(), fields);
    }

    // The process the members defined in our class
    Field[] declaredFields = clazz.getDeclaredFields();
    if (declaredFields != null && declaredFields.length > 0) {
      for (Field f : declaredFields) {
        // Screen out static fields - we don't need to show these in the config
        // Screen out transient fields
        if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
          if (!fields.contains(f))
            fields.add(f);
        }
      }
    }
    return fields;
  }
  
  /**
   * Gets the fields from the class that match the given type. Note that the fields can be superclass or interface of the given parameter.
   * @param parentClass - Class in which to perform search
   * @param fieldTypeClass - find fields that this class can be assigned to.
   * @return Set<Fields> there might be more than a single match hence the use of set
   */
  static Set<Field> getClassFieldByType(Class<?> parentClass, Class<?> fieldTypeClass) {
    if (fieldTypeClass == null)
      return null;
    log.trace("Searching for fields of type: {} within class: {}", fieldTypeClass.getSimpleName(), parentClass.getSimpleName());
    List<Field> fields = new ArrayList<>();
    Set<Field> resultsSet = new HashSet<>();
    boolean isCollection = Collection.class.isAssignableFrom(fieldTypeClass);
    Set<Class<?>> hierarchicalTypesForFieldClass = new HashSet<>();
    hierarchicalTypesForFieldClass = getGenericHierarchicalTypesForClass(fieldTypeClass, hierarchicalTypesForFieldClass);

    getFieldsForClassEnsuringUniqueFieldNames(parentClass, fields);
    for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
      Field field = (Field) iterator.next();
      if (field.getType().isAssignableFrom(fieldTypeClass)) {
        if (isCollection) {
          Set<Class<?>> currentGenericHierarchicalTypesForField = getGenericHierarchicalTypesForField(field);
          if (hierarchicalTypesForFieldClass != null && currentGenericHierarchicalTypesForField != null) {
            currentGenericHierarchicalTypesForField.retainAll(hierarchicalTypesForFieldClass);
            String join = StringUtils.join(currentGenericHierarchicalTypesForField, ',');
            log.trace("Intersect of generic types: '{}'", join);
            if (currentGenericHierarchicalTypesForField.size() > 0) {
              log.trace("Matched collection field: {} of type: {}", field.getName(), field.getType());
              resultsSet.add(field);
            }
          }
          else {
            log.trace("Matched standard collection field: {} of type: {}", field.getName(), field.getType());
            resultsSet.add(field);
          }
        }
        else {
          log.trace("Matched field: {} of type: {}", field.getName(), field.getType());
          resultsSet.add(field);
        }
      }
//       Generic collection types should be matched via XStream functionality
//       Test for a generic type
      else if (field.getGenericType() != field.getType()) {
        Set<Class<?>> genericHierarchicalTypesForField = getGenericHierarchicalTypesForField(field);
        for (Iterator<Class<?>> genericTypeIter = genericHierarchicalTypesForField.iterator(); genericTypeIter.hasNext();) {
          Class<?> genericType = (Class<?>) genericTypeIter.next();
          if (genericType.isAssignableFrom(fieldTypeClass)) {
            resultsSet.add(field);
          }
        }
      }
    }
    log.trace("Return set of {} matched fields", resultsSet.size());
    return resultsSet;
  }
  
  /**
   * Finds a matching field from the parent class that matches the fieldClass & has not yet been processed. This is a first matching case wins approach.
   * @param parentClass - Parent Class for which to search for fields (including all inherited fields)
   * @param fieldClass - Field class for which to find matching fields from the parent class 
   * @param seenProperties - List of fields of the parentClass that have already been processed
   * @return Matching field from the parent class that matches the fieldClass parent and that has not yet been processed or null
   */
  public static Field getMatchedFieldFromClass(Class<?> parentClass, Class<?> fieldClass, final Set<FastField> seenProperties) {
    Set<Field> classFieldByType = getClassFieldByType(parentClass, fieldClass);
    if (classFieldByType == null) return null;
    for (Iterator<Field> iterator = classFieldByType.iterator(); iterator.hasNext();) {
      Field currentMatchedField = (Field) iterator.next();

      // Have we matched this field already? Remember that a class can have multiple fields of a given class.
      // Query the processed fields set
      String fieldCapturedName = currentMatchedField.getName();
      FastField fastField = new FastField(parentClass, fieldCapturedName);
      if (!seenProperties.contains(fastField)) {
        return currentMatchedField;
      }
      else {
        log.trace("Field of type: {} has already been matched to class field: {}, continuing field match search", currentMatchedField.getType(), fieldCapturedName);
      }
    }
    return null;
  }

//  /**
//   * Obtains all generic type information associated with the given class by examining the entire class hierarchy. 
//   * @param clazz - class to process - most likely a collection
//   * @param fields - set to populate with any found generic types
//   * @return - the same set instance as passed in
//   */
//  public static Set<Class<?>> getGenericsClassTypesForClass(Class<?> clazz, Set<Class<?>> genericClasses) {
//    // Process the parents members first
//    if (clazz.getSuperclass() != null) {
//      getGenericsClassTypesForClass(clazz.getSuperclass(), genericClasses);
//    }
//    
//    Type[] genericInterfaces = clazz.getGenericInterfaces();
//    if (genericInterfaces != null) {
//      for (Type classType : genericInterfaces) {
//        if (classType instanceof ParameterizedTypeImpl) {
//          Class<?> innerWrapperType = ((ParameterizedTypeImpl)classType).getRawType();
//          genericClasses.add(innerWrapperType);
//          Type[] innerTypeArguments = ((ParameterizedTypeImpl)classType).getActualTypeArguments();
//          try {
//          for (Type innerType : innerTypeArguments) {
//            genericClasses.add((Class<?>) innerType);  
//          }
//          } catch (ClassCastException e) {
//            log.error("Unexpected error", e);
//            throw e;
//          }
//        }
//        // Handle any generic with a wildcard
//        else if (classType instanceof WildcardTypeImpl) {
//          Type[] upperBounds = ((WildcardTypeImpl)classType).getUpperBounds();
//          for (Type boundClass : upperBounds) {
//            genericClasses.add((Class<?>) boundClass); 
//          }
//        }
//        // Handle a simple class
//        else if (classType instanceof Class<?>) {
//          genericClasses.add((Class<?>) classType); 
//        }
//      }
//    }
//
//    
//    return genericClasses;
//  }
  
//  /**
//   * Gets the generic types for the field based on the field's declaration.
//   * eg if given Map<String, Integer>, this would return the list: [String, Integer]
//   * @param currentField
//   * @return ArrayList of specified generic types, null or empty collection
//   */
//  public static List<Class<?>> getGenericTypesForField(Field currentField) {
//    if (currentField == null)
//      return null;
//    
//    List<Class<?>> resultClassList = new ArrayList<>();
//    
//    Type genericType = currentField.getGenericType();
//    // Where the currentField.isClassField() == false this will be null
//    if (genericType == null)
//      return null;
//    
//    if (genericType instanceof ParameterizedTypeImpl) {
//      Type[] actualTypeArguments = ((ParameterizedTypeImpl)genericType).getActualTypeArguments();
//      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
//        for (Type classType : actualTypeArguments) {
//          // Handle a possible second level of generics information
//          if (classType instanceof ParameterizedTypeImpl) {
//            Class<?> innerWrapperType = ((ParameterizedTypeImpl)classType).getRawType();
//            resultClassList.add(innerWrapperType);
//            Type[] innerTypeArguments = ((ParameterizedTypeImpl)classType).getActualTypeArguments();
//            try {
//            for (Type innerType : innerTypeArguments) {
//              resultClassList.add((Class<?>) innerType);  
//            }
//            } catch (ClassCastException e) {
//              log.error("Unexpected error", e);
//              throw e;
//            }
//          }
//          // Handle any generic with a wildcard
//          else if (classType instanceof WildcardTypeImpl) {
//            Type[] upperBounds = ((WildcardTypeImpl)classType).getUpperBounds();
//            for (Type boundClass : upperBounds) {
//              resultClassList.add((Class<?>) boundClass); 
//            }
//          }
//          // Handle a simple class
//          else if (classType instanceof Class<?>) {
//            resultClassList.add((Class<?>) classType); 
//          }
//        }
//      }
//    }
//    else {
//      if (!genericType.getClass().equals(java.lang.Class.class))
//        log.error("Unexpected generic type encountered: {}, defaulting to object for field: {} in class: {}", genericType.getClass(), currentField.getName(), currentField.getDeclaringClass());
//      resultClassList.add(Object.class);
//    }
//    return resultClassList;
//  }
  
  /**
   * Given a genericType object, this method obtains the actual class from within it.
   * @param genericType
   * @param List to populate
   * @return List<Class> that contains all type classes from with the genericType given
   */
  static List<Class<?>> getClassFromGenericType(Type genericType, List<Class<?>> resultClassList) {
    if (resultClassList == null)
      resultClassList = new ArrayList<>();
    
    if (genericType instanceof ParameterizedTypeImpl) {
      Type[] actualTypeArguments = ((ParameterizedTypeImpl)genericType).getActualTypeArguments();
      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
        for (Type classType : actualTypeArguments) {
          // Handle a possible second level of generics information
          if (classType instanceof ParameterizedTypeImpl) {
            Class<?> innerWrapperType = ((ParameterizedTypeImpl)classType).getRawType();
            resultClassList.add(innerWrapperType);
            Type[] innerTypeArguments = ((ParameterizedTypeImpl)classType).getActualTypeArguments();
            try {
              for (Type innerType : innerTypeArguments) {
                resultClassList.add((Class<?>) innerType);  
              }
            } catch (ClassCastException e) {
              log.error("Unexpected error", e);
              throw e;
            }
          }
          // Handle any generic with a wildcard
          else if (classType instanceof WildcardTypeImpl) {
            Type[] upperBounds = ((WildcardTypeImpl)classType).getUpperBounds();
            for (Type boundClass : upperBounds) {
              resultClassList.add((Class<?>) boundClass); 
            }
          }
          // Handle a simple class
          else if (classType instanceof Class<?> &&
              !(classType instanceof TypeVariableImpl)) {
            resultClassList.add((Class<?>) classType); 
          }
        }
      }
    }
    else if (genericType instanceof Object) {
      Class<?> classType = (Class<?>) genericType;
      if (!Collection.class.isAssignableFrom(classType)) {
        resultClassList.add(classType);
      }
    }
    
    return resultClassList;
  }
  
  /**
   * Obtains all generic type information associated with the given Field by
   * either checking it for any Generic declarations or examining it's entire
   * class hierarchy.
   * 
   * @param f - field to process
   * @return - Set of Classes or null
   */
  static Set<Class<?>> getGenericHierarchicalTypesForField(Field f) {
    List<Class<?>> genericTypesForField = new ArrayList<>();
    
    // 1) Handle the case where the variable is a declared with a generic type
    Type genericType = f.getGenericType();
    getClassFromGenericType(genericType, genericTypesForField);
    
    // 2) Handle the case where the variable type might have a generic type somewhere in it's class hierarchy
    Set<Class<?>> genericHierarchicalTypesForClass = new HashSet<>();
    genericHierarchicalTypesForClass = getGenericHierarchicalTypesForClass(f.getType(), genericHierarchicalTypesForClass);
    
    // Convert the list to a set if it has any data
    Set<Class<?>> hashSet = new HashSet<>(); 
    if (genericTypesForField != null && genericTypesForField.size() > 0) {
      hashSet = new HashSet<Class<?>>(genericTypesForField);
    }
    if (genericHierarchicalTypesForClass != null && genericHierarchicalTypesForClass.size() > 0) {
      hashSet.addAll(genericHierarchicalTypesForClass);
    }
    if (hashSet.size() == 0)
      return null;
    return hashSet;
  }
  
  /**
   * Obtains all generic type information associated with the given Class by
   * examining it's entire class hierarchy.
   * 
   * @param clazz - class to process - most likely a collection
   * @return - Set of Classes or null
   */
  static Set<Class<?>> getGenericHierarchicalTypesForClass(Class<?> clazz, Set<Class<?>> genericTypeSet) {
    log.trace("getGenericHierarchicalTypesForClass entry({})", clazz.getSimpleName());
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null && superclass != Object.class) {
      getGenericHierarchicalTypesForClass(superclass, genericTypeSet);
    }
    
    List<Class<?>> genericTypesForField = new ArrayList<>();
    // Try Generic interfaces
    Type[] genericSuperTypes = clazz.getGenericInterfaces();
    if (genericSuperTypes != null && genericSuperTypes.length > 0) {
      for (Type type : genericSuperTypes) {
        log.trace("Processing type: {}", type);
        getClassFromGenericType(type, genericTypesForField);
      }
      // Convert the list to a set if it has any data
      if (genericTypesForField != null && genericTypesForField.size() > 0) {
        genericTypeSet.addAll(genericTypesForField);
      }
    }
    
    return genericTypeSet;
  }
  
  /**
   * Finds a collection field within the given parent class that can take the given itemType.
   * @param parentClass - Class that is currently being processed
   * @param itemType - Class member that we are trying to match within parent class.
   * @return String - Name of field from parent class that can take the given itemType.
   */
  public static String getImplicitCollectionFieldNameForType(Class parentClass, Class itemType) {
    log.trace("Searching for implict collection field that takes type: {} within class: {}", itemType.getSimpleName(), parentClass.getSimpleName());
    List<Field> fields = new ArrayList<>();
    getFieldsForClassEnsuringUniqueFieldNames(parentClass, fields);
    for (Iterator<Field> iterator = fields.iterator(); iterator.hasNext();) {
      Field field = (Field) iterator.next();
      boolean isCollection = Collection.class.isAssignableFrom(field.getType());
      boolean isGenericCollection = field.getGenericType() != field.getType();
      // Only handle generic collections, since a plain list field would have been matched already.
      if (isCollection && isGenericCollection) {
        Set<Class<?>> genericHierarchicalTypesForField = getGenericHierarchicalTypesForField(field);
        for (Iterator<Class<?>> genericTypeIter = genericHierarchicalTypesForField.iterator(); genericTypeIter.hasNext();) {
          Class<?> genericType = (Class<?>) genericTypeIter.next();
          if (genericType.isAssignableFrom(itemType)) {
            return field.getName();
          }
        } // end for
      } // end if
    } // end for
    return null;
  } // end method
}
