package com.adaptris.core.common;

import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;

public interface MultiPayloadDataInputParameter<T> extends DataInputParameter<T>
{
	String getPayloadId();

	void setPayloadId(String id);

	T extract(InterlokMessage m) throws InterlokException;

	T extract(String id, MultiPayloadAdaptrisMessage m) throws InterlokException;
}
