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

package com.adaptris.core.marshaller.xstream;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.ReferencingMarshallingContext;
import com.thoughtworks.xstream.core.util.ArrayIterator;
import com.thoughtworks.xstream.core.util.FastField;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.core.util.Primitives;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * XStream Converter that marshals objects to XML using Reflection.
 * <p>
 * This class allows us to marshal an object using it's alias as the main xml element rather than the default XStream way of
 * specifying the subclass as an attribute.
 * </p>
 * 
 * <pre>
 * {@code
 * Instead of the following:
 * <standard-workflow>
 *      <consumer class="null-message-consumer">
 *      
 * This class outputs:
 * <standard-workflow>
 *      <null-message-consumer>
 * }
 * </pre>
 * 
 * @author bklair
 */
public class AliasedElementReflectionConverter extends ReflectionConverter {

  protected transient Logger log = LoggerFactory.getLogger(AliasedJavaBeanConverter.class);
  
  private transient ReflectionProvider pureJavaReflectionProvider;
  
  
	public AliasedElementReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
		this.pureJavaReflectionProvider = reflectionProvider;
	}

  protected void doMarshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
    final List<FieldInfo> fields = new ArrayList<>();
    final Map<String, java.lang.reflect.Field> defaultFieldDefinition = new HashMap<>();

    // Attributes might be preferred to child elements ...
    reflectionProvider.visitSerializableFields(source,
        new ReflectionProvider.Visitor() {
          final Set<String> writtenAttributes = new HashSet<>();

          public void visit(String fieldName, Class type, Class definedIn, Object value) {
            if (!mapper.shouldSerializeMember(definedIn, fieldName)) {
              return;
            }
            if (!defaultFieldDefinition.containsKey(fieldName)) {
              Class lookupType = source.getClass();
              // See XSTR-457 and OmitFieldsTest
              if (definedIn != source.getClass() && !mapper.shouldSerializeMember(lookupType, fieldName)) {
                lookupType = definedIn;
              }
              defaultFieldDefinition.put(fieldName, reflectionProvider.getField(lookupType, fieldName));
            }

            SingleValueConverter converter = mapper.getConverterFromItemType(fieldName, type, definedIn);
            if (converter != null) {
              final String attribute = mapper.aliasForAttribute(mapper.serializedMember(definedIn, fieldName));
              if (value != null) {
                if (writtenAttributes.contains(fieldName)) { // TODO: use attribute
                  throw new ConversionException(
                      "Cannot write field with name '" + fieldName
                          + "' twice as attribute for object of type "
                          + source.getClass().getName());
                }
                final String str = converter.toString(value);
                if (str != null) {
                  writer.addAttribute(attribute, str);
                }
              }
              writtenAttributes.add(fieldName); // TODO: use attribute
            }
            else {
              fields.add(new FieldInfo(fieldName, type, definedIn, value));
            }
          }
        });

    new Object() {
      {
        for (Iterator<FieldInfo> fieldIter = fields.iterator(); fieldIter
            .hasNext();) {
          FieldInfo info = (FieldInfo) fieldIter.next();
          // Check if the field is not null, we don't output null fields
          if (info.value != null) {
            Mapper.ImplicitCollectionMapping mapping = mapper
                .getImplicitCollectionDefForFieldName(source.getClass(),
                    info.fieldName);
            if (mapping != null) {
              if (context instanceof ReferencingMarshallingContext) {
                if (info.value != Collections.EMPTY_LIST
                    && info.value != Collections.EMPTY_SET
                    && info.value != Collections.EMPTY_MAP) {
                  ReferencingMarshallingContext refContext = (ReferencingMarshallingContext) context;
                  refContext.registerImplicit(info.value);
                }
              }
              final boolean isCollection = info.value instanceof Collection;
              final boolean isMap = info.value instanceof Map;
              final boolean isEntry = isMap
                  && mapping.getKeyFieldName() == null;
              final boolean isArray = info.value.getClass().isArray();

              for (Iterator iter = isArray ? new ArrayIterator(info.value)
                  : isCollection ? ((Collection) info.value).iterator()
                      : isEntry ? ((Map) info.value).entrySet().iterator()
                          : ((Map) info.value).values().iterator(); iter
                  .hasNext();) {
                Object obj = iter.next();
                final String itemName;
                final Class itemType;
                if (obj == null) {
                  itemType = Object.class;
                  itemName = mapper.serializedClass(null);
                } else if (isEntry) {
                  final String entryName = mapping.getItemFieldName() != null ? mapping
                      .getItemFieldName() : mapper
                      .serializedClass(Map.Entry.class);
                  Map.Entry entry = (Map.Entry) obj;
                  ExtendedHierarchicalStreamWriterHelper.startNode(writer,
                      entryName, entry.getClass());
                  writeItem(entry.getKey(), context, writer);
                  writeItem(entry.getValue(), context, writer);
                  writer.endNode();
                  continue;
                } else if (mapping.getItemFieldName() != null) {
                  itemType = mapping.getItemType();
                  itemName = mapping.getItemFieldName();
                } else {
                  itemType = obj.getClass();
                  itemName = mapper.serializedClass(itemType);
                }
                writeField(info.fieldName, itemName, itemType, info.definedIn,
                    obj);
              }
            }
            // Field is not an implicit collection
            else {
              writeField(info.fieldName, null, info.type, info.definedIn,
                  info.value);
            }
          }
        }
      }

//      void writeFieldStandard(String fieldName, String aliasName, Class fieldType, Class definedIn, Object newObj) {
//        Class actualType = newObj != null ? newObj.getClass() : fieldType;
//        ExtendedHierarchicalStreamWriterHelper.startNode(
//            writer,
//            aliasName != null ? aliasName : mapper.serializedMember(
//                source.getClass(), fieldName), actualType);
//
//        // We don't process null fields (field values)
//        if (newObj != null) {
//          Class defaultType = mapper.defaultImplementationOf(fieldType);
//          if (!actualType.equals(defaultType)) {
//            String serializedClassName = mapper.serializedClass(actualType);
//            if (!serializedClassName
//                .equals(mapper.serializedClass(defaultType))) {
//              String attributeName = mapper.aliasForSystemAttribute("class");
//              if (attributeName != null) {
//                writer.addAttribute(attributeName, serializedClassName);
//              }
//            }
//          }
//
//          final Field defaultField = (Field) defaultFieldDefinition
//              .get(fieldName);
//          if (defaultField.getDeclaringClass() != definedIn) {
//            String attributeName = mapper.aliasForSystemAttribute("defined-in");
//            if (attributeName != null) {
//              writer.addAttribute(attributeName,
//                  mapper.serializedClass(definedIn));
//            }
//          }
//
//          Field field = reflectionProvider.getField(definedIn, fieldName);
//          marshallField(context, newObj, field);
//        }
//        writer.endNode();
//      }

      // Modified version of method from that super class
      void writeField(String fieldName, String aliasName, Class fieldType, Class definedIn, Object newObj) {
        Class<?> actualType = newObj != null ? newObj.getClass() : fieldType;
        String elementName = aliasName != null ? aliasName : mapper.serializedMember(source.getClass(), fieldName);

        String classAttributeName = null;
        String definedAttributeName = null;
        // We don't process null fields (field values)
        if (newObj != null) {
          Class defaultType = mapper.defaultImplementationOf(fieldType);
          if (!actualType.equals(defaultType)) {
            String serializedClassName = mapper.serializedClass(actualType);
            if (!serializedClassName.equals(mapper.serializedClass(defaultType))) {
              classAttributeName = mapper.aliasForSystemAttribute("class");
            }
          }

          final Field defaultField = (Field) defaultFieldDefinition.get(fieldName);
          if (defaultField.getDeclaringClass() != definedIn) {
            definedAttributeName = mapper.aliasForSystemAttribute("defined-in");
          }
        }

        writeOutElementBasedOnClassType(elementName, definedIn, actualType, classAttributeName, definedAttributeName);
        if (newObj != null) {
          Field field = reflectionProvider.getField(definedIn, fieldName);
          marshallField(context, newObj, field);
        }
        writer.endNode();
      }

      // Now where the super class would have written out the field name
      // followed by a class attribute that contained the subclass name,
      // we will write out the subclass alias name as the element with no
      // class attribute at all.
      private void writeOutElementBasedOnClassType(String elementName, Class definedIn, Class<?> actualType, String classAttributeName,String definedAttributeName) {
        boolean isClassAttributeSet = !StringUtils.isBlank(classAttributeName);
        if (isClassAttributeSet) {
          String serializedClassName = mapper.serializedClass(actualType);
          ExtendedHierarchicalStreamWriterHelper.startNode(writer, serializedClassName, actualType);
        }
        else {
          String serializedClassName = mapper.serializedClass(actualType);

          ExtendedHierarchicalStreamWriterHelper.startNode(writer, elementName, actualType);
          if (classAttributeName != null) {
            writer.addAttribute(classAttributeName, serializedClassName);
          }
          if (definedAttributeName != null) {
            writer.addAttribute(definedAttributeName, mapper.serializedClass(definedIn));
          }
        }
      }

      void writeItem(Object item, MarshallingContext context, HierarchicalStreamWriter writer) {
        if (item == null) {
          String name = mapper.serializedClass(null);
          ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, Mapper.Null.class);
          writer.endNode();
        }
        else {
          String name = mapper.serializedClass(item.getClass());
          ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, item.getClass());
          context.convertAnother(item);
          writer.endNode();
        }
      }
    };
	}

  /**
   * Unmarshall the XML data from the reader and create the object instance.
   */
  @Override
  public Object doUnmarshal(final Object result, final HierarchicalStreamReader reader, final UnmarshallingContext context) {
    final Class resultType = result.getClass();
    final Set<FastField> seenFields = new HashSet() {
      public boolean add(Object e) {
        if (!super.add(e)) {
          throw new DuplicateFieldException(((FastField) e).getName());
        }
        return true;
      }
    };

    // process attributes before recursing into child elements.
    Iterator<?> it = reader.getAttributeNames();
    while (it.hasNext()) {
      String attrAlias = (String) it.next();
      // TODO: realMember should return FastField
      String attrName = mapper.realMember(resultType, mapper.attributeForAlias(attrAlias));
      Field field = reflectionProvider.getFieldOrNull(resultType, attrName);
      if (field != null && shouldUnmarshalField(field)) {
        Class classDefiningField = field.getDeclaringClass();
        if (!mapper.shouldSerializeMember(classDefiningField, attrName)) {
          continue;
        }

        // we need a converter that produces a string representation only
        SingleValueConverter converter = mapper.getConverterFromAttribute(
            classDefiningField, attrName, field.getType());
        Class type = field.getType();
        if (converter != null) {
          Object value = converter.fromString(reader.getAttribute(attrAlias));
          if (type.isPrimitive()) {
            type = Primitives.box(type);
          }
          if (value != null && !type.isAssignableFrom(value.getClass())) {
            throw new ConversionException("Cannot convert type "
                + value.getClass().getName() + " to type " + type.getName());
          }
          seenFields.add(new FastField(classDefiningField, attrName));
          reflectionProvider.writeField(result, attrName, value, classDefiningField);
        }
      }
    }

    Map implicitCollectionsForCurrentObject = null;
    // Process the child nodes of the current node
    while (reader.hasMoreChildren()) {
      boolean validMatchFound = false;
      reader.moveDown();

      String originalNodeName = reader.getNodeName();
      log.trace("Reading processing node: {}", originalNodeName);
      Class explicitDeclaringClass = readDeclaringClass(reader);
      Class fieldDeclaringClass = explicitDeclaringClass == null ? resultType : explicitDeclaringClass;
      String fieldName = mapper.realMember(fieldDeclaringClass, originalNodeName);
      Mapper.ImplicitCollectionMapping implicitCollectionMapping = mapper
          .getImplicitCollectionDefForFieldName(fieldDeclaringClass, fieldName);
      final Object value;
      String implicitFieldName = null;
      Field field = null;
      Class type = null;

      // Handle standard field ie one not marked as implicit
      if (implicitCollectionMapping == null) {
        // no item of an implicit collection for this name ... do we have a
        // field?
        field = reflectionProvider.getFieldOrNull(fieldDeclaringClass, fieldName);
        if (field == null) {
          // it is not a field ... do we have a field alias?
          Class itemType = mapper.getItemTypeForItemFieldName(resultType, fieldName);
          if (itemType != null) {
            String classAttribute = HierarchicalStreams.readClassAttribute( reader, mapper);
            if (classAttribute != null) {
              type = mapper.realClass(classAttribute);
            } else {
              type = itemType;
            }
          } else {
            // it is not an alias ... do we have an element of an implicit
            // collection based on type only?
            try {
              type = mapper.realClass(originalNodeName);
              implicitFieldName = mapper.getFieldNameForItemTypeAndName(
                  context.getRequiredType(), type, originalNodeName);
              // If there is an implicit collection with the
              // itemFieldName set, then we must bypass XStream's
              // implicit collection matching because it will not
              // match the subclass name to the itemFieldName!
              if (implicitFieldName == null) {
                implicitFieldName = XStreamUtils.getImplicitCollectionFieldNameForType(context.getRequiredType(), type);
              }
            } catch (CannotResolveClassException e) {
              // type stays null ...
            }
            
            if (type == null || implicitFieldName == null) {
              Field matchedField = XStreamUtils.getMatchedFieldFromClass(resultType, type, seenFields);
              if (matchedField != null) {
                field = matchedField;
                fieldName = matchedField.getName();
                validMatchFound = true;
              }

              if (!validMatchFound) {
                // either not a type or element is a type alias, but does not
                // belong to an implicit field
                handleUnknownField(explicitDeclaringClass, fieldName, resultType, originalNodeName);

                // element is unknown in declaring class, ignore it now
                type = null;
              }
            }
          }

          if (validMatchFound) {
             value = unmarshallField(context, result, type, field);
//            value = reflectionProvider.newInstance(type); // TODO perhaps call
                                                          // context.convertAnother(result,
                                                          // type);
          } else if (type == null) {
            // no type, no value
            value = null;
          } else {
            if (Map.Entry.class.equals(type)) {
              // it is an element of an implicit map with two elements now forkey and value
              reader.moveDown();
              final Object key = context.convertAnother(result,
                  HierarchicalStreams.readClassType(reader, mapper));
              reader.moveUp();
              reader.moveDown();
              final Object v = context.convertAnother(result, HierarchicalStreams.readClassType(reader, mapper));
              reader.moveUp();
              value = Collections.singletonMap(key, v).entrySet().iterator().next();
            } else {
              // recurse info hierarchy
              value = context.convertAnother(result, type);
            }
          }
        }
        // Handle valid class field
        else {
          boolean fieldAlreadyChecked = false;

          // we have a field, but do we have to address a hidden one?
          if (explicitDeclaringClass == null) {
            while (field != null
                && !(fieldAlreadyChecked = shouldUnmarshalField(field)
                    && mapper.shouldSerializeMember(field.getDeclaringClass(),
                        fieldName))) {
              field = reflectionProvider.getFieldOrNull(field
                  .getDeclaringClass().getSuperclass(), fieldName);
            }
          }
          if (field != null
              && (fieldAlreadyChecked || (shouldUnmarshalField(field) && mapper
                  .shouldSerializeMember(field.getDeclaringClass(), fieldName)))) {

            String classAttribute = HierarchicalStreams.readClassAttribute(
                reader, mapper);
            if (classAttribute != null)
              log.trace("Reading class attribute: {}", classAttribute);
            if (classAttribute != null) {
              type = mapper.realClass(classAttribute);
            } else {
              type = mapper.defaultImplementationOf(field.getType());
            }
            // TODO the reflection provider should already return the proper field
            value = unmarshallField(context, result, type, field);
            Class definedType = field.getType();
            if (!definedType.isPrimitive()) {
              type = definedType;
            }
          } else {
            value = null;
          }
        }
      }
      // Handle collection field with implicit mapping
      else {
        // we have an implicit collection with defined names
        implicitFieldName = implicitCollectionMapping.getFieldName();
        type = implicitCollectionMapping.getItemType();
        if (type == null) {
          String classAttribute = HierarchicalStreams.readClassAttribute(
              reader, mapper);
          type = mapper.realClass(classAttribute != null ? classAttribute
              : originalNodeName);
        }
        value = context.convertAnother(result, type);
      }

      if (value != null && !type.isAssignableFrom(value.getClass())) {
        throw new ConversionException("Cannot convert type "
            + value.getClass().getName() + " to type " + type.getName());
      }

      if (field != null) {
        log.trace("Updating field value for field: {}", fieldName);
        if (validMatchFound) {
          reflectionProvider.writeField(result, fieldName, value,
              field.getDeclaringClass());
        } else {
          reflectionProvider.writeField(result, fieldName, value,
              field.getDeclaringClass());
        }
        seenFields.add(new FastField(field.getDeclaringClass(), fieldName));
      } else if (type != null) {
        if (implicitFieldName == null) {
          // look for implicit field
          implicitFieldName = mapper.getFieldNameForItemTypeAndName(context
              .getRequiredType(), value != null ? value.getClass()
              : Mapper.Null.class, originalNodeName);
        }
        if (implicitCollectionsForCurrentObject == null) {
          implicitCollectionsForCurrentObject = new HashMap();
        }
        writeValueToImplicitCollection(value,
            implicitCollectionsForCurrentObject, result, implicitFieldName);
      }

      reader.moveUp();
    }

    if (implicitCollectionsForCurrentObject != null) {
      for (Iterator iter = implicitCollectionsForCurrentObject.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        Object value = entry.getValue();
        if (value instanceof ArraysList) {
          Object array = ((ArraysList) value).toPhysicalArray();
          reflectionProvider.writeField(result, (String) entry.getKey(), array,
              null);
        }
      }
    }

    return result;
  }

  // Copied from parent
  private Class readDeclaringClass(HierarchicalStreamReader reader) {
    String attributeName = mapper.aliasForSystemAttribute("defined-in");
    String definedIn = attributeName == null ? null : reader.getAttribute(attributeName);
    return definedIn == null ? null : mapper.realClass(definedIn);
  }

  // Copied from parent
  private void handleUnknownField(Class classDefiningField, String fieldName, Class resultType, String originalNodeName) {
    if (classDefiningField == null) {
      for (Class cls = resultType; cls != null; cls = cls.getSuperclass()) {
        if (!mapper.shouldSerializeMember(cls, originalNodeName)) {
          return;
        }
      }
    }
    throw new UnknownFieldException(resultType.getName(), fieldName);
  }

  // Copied from parent
  private void writeValueToImplicitCollection(Object value, Map implicitCollections, Object result, String implicitFieldName) {
    Collection collection = (Collection)implicitCollections.get(implicitFieldName);
    if (collection == null) {
      Class physicalFieldType = reflectionProvider.getFieldType(
          result, implicitFieldName, null);
      if (physicalFieldType.isArray()) {
        collection = new ArraysList(physicalFieldType);
      } else {
        Class fieldType = mapper.defaultImplementationOf(physicalFieldType);
        if (!(Collection.class.isAssignableFrom(fieldType) || Map.class
            .isAssignableFrom(fieldType))) {
          throw new ObjectAccessException(
              "Field "
                  + implicitFieldName
                  + " of "
                  + result.getClass().getName()
                  + " is configured for an implicit Collection or Map, but field is of type "
                  + fieldType.getName());
        }
        if (pureJavaReflectionProvider == null) {
          pureJavaReflectionProvider = new PureJavaReflectionProvider();
        }
        Object instance = pureJavaReflectionProvider.newInstance(fieldType);
        if (instance instanceof Collection) {
          collection = (Collection)instance;
        } else {
          Mapper.ImplicitCollectionMapping implicitCollectionMapping = mapper
              .getImplicitCollectionDefForFieldName(result.getClass(), implicitFieldName);
          collection = new MappingList(
              (Map)instance, implicitCollectionMapping.getKeyFieldName());
        }
        reflectionProvider.writeField(result, implicitFieldName, instance, null);
      }
      implicitCollections.put(implicitFieldName, collection);
    }
    collection.add(value);
  }

  // TODO remove once debugging is done
  // Copied from parent class
  @Override
  protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String attributeName = mapper.aliasForSystemAttribute("resolves-to");
    String readResolveValue = attributeName == null ? null : reader.getAttribute(attributeName);
    Object currentObject = context.currentObject();
    if (currentObject != null) {
      return currentObject;
    } else if (readResolveValue != null) {
      return reflectionProvider.newInstance(mapper.realClass(readResolveValue));
    } else {
      Class requiredType = context.getRequiredType();
      return reflectionProvider.newInstance(requiredType);
    }
  }

  // Copied from parent class
  private static class FieldInfo {
    final String fieldName;
    final Class type;
    final Class definedIn;
    final Object value;

    FieldInfo(String fieldName, Class type, Class definedIn, Object value) {
      this.fieldName = fieldName;
      this.type = type;
      this.definedIn = definedIn;
      this.value = value;
    }
  }

  // Copied from parent class
  private static class ArraysList extends ArrayList {
    final Class physicalFieldType;

    ArraysList(Class physicalFieldType) {
      this.physicalFieldType = physicalFieldType;
    }

    Object toPhysicalArray() {
      Object[] objects = toArray();
      Object array = Array.newInstance(
          physicalFieldType.getComponentType(), objects.length);
      if (physicalFieldType.getComponentType().isPrimitive()) {
        for (int i = 0; i < objects.length; ++i) {
          Array.set(array, i, Array.get(objects, i));
        }
      } else {
        System.arraycopy(objects, 0, array, 0, objects.length);
      }
      return array;
    }
  }

  // Copied from parent class
  private class MappingList extends AbstractList {

    private final Map map;
    private final String keyFieldName;
    private final Map fieldCache = new HashMap();

    public MappingList(Map map, String keyFieldName) {
      this.map = map;
      this.keyFieldName = keyFieldName;
    }

    public boolean add(Object object) {
      if (object == null) {
        boolean containsNull = !map.containsKey(null);
        map.put(null, null);
        return containsNull;
      }
      Class itemType = object.getClass();

      if (keyFieldName != null) {
        Field field = (Field)fieldCache.get(itemType);
        if (field == null) {
          field = reflectionProvider.getField(itemType, keyFieldName);
          fieldCache.put(itemType, field);
        }
        if (field != null) {
          try {
            Object key = field.get(object);
            return map.put(key, object) == null;
          } catch (IllegalArgumentException e) {
            throw new ObjectAccessException("Could not get field "
                + field.getClass()
                + "."
                + field.getName(), e);
          } catch (IllegalAccessException e) {
            throw new ObjectAccessException("Could not get field "
                + field.getClass()
                + "."
                + field.getName(), e);
          }
        }
      }
      else if (object instanceof Map.Entry) {
        final Map.Entry entry = (Map.Entry)object;
        return map.put(entry.getKey(), entry.getValue()) == null;
      }

      throw new ConversionException("Element of type "
          + object.getClass().getName()
          + " is not defined as entry for map of type "
          + map.getClass().getName());
    }

    public Object get(int index) {
      throw new UnsupportedOperationException();
    }

    public int size() {
      return map.size();
    }
  }
}
