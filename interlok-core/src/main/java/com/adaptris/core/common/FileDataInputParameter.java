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

package com.adaptris.core.common;

import static com.adaptris.util.URLHelper.connect;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* {@code DataInputParameter} implementation that reads from a file.
*
* @config file-data-input-parameter
*
*/
@JacksonXmlRootElement(localName = "file-data-input-parameter")
@XStreamAlias("file-data-input-parameter")
@DisplayOrder(order = {"destination"})
public class FileDataInputParameter extends FileParameter
implements DataInputParameter<String> {

public FileDataInputParameter() {

}

@Override
public String extract(InterlokMessage message) throws CoreException {
try {
return load(new URLString(url(message)), message.getContentEncoding());
} catch (IOException ex) {
throw ExceptionHelper.wrapCoreException(ex);
}
}

protected String load(URLString loc, String encoding) throws IOException {
String content = null;

try (InputStream inputStream = connect(loc)) {
StringWriter writer = new StringWriter();
IOUtils.copy(inputStream, writer, encoding);
content = writer.toString();
}
return content;
}
}
