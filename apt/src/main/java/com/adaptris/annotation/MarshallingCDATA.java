package com.adaptris.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Internal annotation that emits a CDATA tag wrapping a field when generating example-xml.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MarshallingCDATA {
	
}
