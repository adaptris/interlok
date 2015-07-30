package com.adaptris.core;


/**
 * 
 * Convenience for getting the default marshalling system currently available in the adapter.
 * 
 * Added a web-service marshaller because the auto-generated web service services currently
 * fail when using the PrettyStaxDriver
 * 
 * @author gcsiki
 */
public class DefaultMarshaller {

	private static AdaptrisMarshaller marshaller;

	public static AdaptrisMarshaller getDefaultMarshaller() throws CoreException {
		if (marshaller == null) {
			// The default marshaller is xstream marshaller for now
			marshaller = new XStreamMarshaller();
		}
		return marshaller;
	}

	public static void setDefaultMarshaller(AdaptrisMarshaller _marshaller) {
		marshaller = _marshaller;
	}

}
