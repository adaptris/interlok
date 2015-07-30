package com.adaptris.core.fs;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.XStreamMarshaller;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config fs-xstream-processed-item-cache
 * @license BASIC
 * 
 * @author dsefton
 * @author $Author: dsefton $
 */
@XStreamAlias("fs-xstream-processed-item-cache")
public class XStreamItemCache extends MarshallingItemCache {
	
	/**
	 * Default Constructor
	 *
	 * @throws Exception
	 */
	public XStreamItemCache() throws Exception {
		super();
	}
	
	public XStreamItemCache(String store) throws Exception {
		super(store);
	}
	
	@Override
	protected AdaptrisMarshaller initMarshaller() throws Exception{
		return new XStreamMarshaller();
	}

}
