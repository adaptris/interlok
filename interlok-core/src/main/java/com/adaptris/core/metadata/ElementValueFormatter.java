package com.adaptris.core.metadata;

import com.adaptris.core.MetadataElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Default element formatter. Format is just the metadata value.
*
* @author Ashley Anderson <ashley.anderson@reedbusiness.com>
*/
@JacksonXmlRootElement(localName = "element-value-formatter")
@XStreamAlias("element-value-formatter")
public class ElementValueFormatter implements ElementFormatter {

/**
* {@inheritDoc}
*/
@Override
public String format(final MetadataElement element) {
return element.getValue();
}
}
