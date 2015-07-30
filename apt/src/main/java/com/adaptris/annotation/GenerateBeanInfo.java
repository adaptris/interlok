package com.adaptris.annotation;

/**
 * Annotation to create a standard BeanInfo java class for any given class.
 * <p>
 * Use this to force XStream to use the setters and getters rather than the member variables directly. This is generally useful if
 * the setters and getters have behaviour associated with them that are not simple <code>this.x = x</code>.
 * </p>
 * 
 * 
 */
public @interface GenerateBeanInfo {

}
