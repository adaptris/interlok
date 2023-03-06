/*
* Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.MetadataCollection;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
* {@link MetadataFilter} implementation that wraps a list of filters.
*
* @config composite-metadata-filter
*
*/
@JacksonXmlRootElement(localName = "composite-metadata-filter")
@XStreamAlias("composite-metadata-filter")
public class CompositeMetadataFilter extends MetadataFilterImpl {

@NotNull
@Valid
@AutoPopulated
@XStreamImplicit
private List<MetadataFilter> filters;

public CompositeMetadataFilter() {
setFilters(new ArrayList<MetadataFilter>());
}

public CompositeMetadataFilter(MetadataFilter... filters) {
this();
setFilters(new ArrayList<MetadataFilter>(Arrays.asList(filters)));
}

@Override
public MetadataCollection filter(MetadataCollection original) {
MetadataCollection result = original;
for (MetadataFilter f : filters) {
result = f.filter(result);
}
return result;
}

/**
* @return the filters
*/
public List<MetadataFilter> getFilters() {
return filters;
}

/**
* @param filters the filters to set
*/
public void setFilters(List<MetadataFilter> filters) {
this.filters = filters;
}

}
