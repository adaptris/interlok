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

package com.adaptris.core.services.jdbc;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.adaptris.util.NumberUtils;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Translate the ResultSet contents into some number of XML messages.
*
* <p>
* The format of the output messages is as follows:
*
* <pre>
* {@code
* <Results>
*   <Row>
*     <column1>...</column1>
*     <column2>...</column2>
*     ...
*   </Row>
*   <Row> ... </Row>
* </Results>
* }
* </pre>
* </p>
* <p>
* Note that column1, column2, etc. is replaced by the actual column name as returned in the query. As such, the column name must be
* a valid XML element name. If the actual name (in the database table definition) is not valid, the query should specify an alias
* name in the query. E.g: <code>SELECT "col 1" AS "col1" FROM mytable;</code>
* </p>
* <p>
* The output messages will be constructed by the same MessageFactory as the incoming message. If a MessageFactory has been configured
* then that one will be used instead (useful in case the incoming message is file-backed, for example).
* </p>
* <p>
* If you want to see how many rows were processed you can set one/both of the following;
* <table>
* <tr>
* <th>Item</th>
* <th>Description</th>
* <th>Value</th>
* </tr>
* <tr>
* <td>result-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows returned by your query</td><td>Metadata item name</td>
* </tr>
* <tr>
* <td>update-count-metadata-item</td><td>If set to a String metadata item name will specify the metadata item to contain the number of rows updated by your SQL statement</td><td>Metadata item name</td>
* </tr>
* </table>
* <p>
*
* @config jdbc-splitting-xml-payload-translator
*
* @author gdries
*
*/
@JacksonXmlRootElement(localName = "jdbc-splitting-xml-payload-translator")
@XStreamAlias("jdbc-splitting-xml-payload-translator")
@DisplayOrder(order = {"maxRowsPerMessage", "copyMetadata", "producer",
"columnNameStyle", "columnTranslators", "mergeImplementation", "outputMessageEncoding",
"stripIllegalXmlChars", "xmlColumnPrefix", "xmlColumnRegexp", "cdataColumnRegexp",
"messageFactory"})
public class SplittingXmlPayloadTranslator extends XmlPayloadTranslatorImpl {
@NotNull(message = "Connection for split messages may not be null")
@AutoPopulated
@Valid
private AdaptrisConnection connection;

@NotNull(message = "Producer for split messages may not be null")
@AutoPopulated
@Valid
private AdaptrisMessageProducer producer;

@AdvancedConfig
private AdaptrisMessageFactory messageFactory;

@InputFieldDefault(value = "false")
private Boolean copyMetadata;

@InputFieldDefault(value = "1000")
private Integer maxRowsPerMessage;

public SplittingXmlPayloadTranslator() {
super();
setConnection(new NullConnection());
setProducer(new NullMessageProducer());
}

@Override
public void init() throws CoreException {
super.init();
connection.addMessageProducer(producer);
LifecycleHelper.init(getConnection());
LifecycleHelper.init(getProducer());
}

@Override
public void start() throws CoreException {
super.start();
LifecycleHelper.start(getConnection());
LifecycleHelper.start(getProducer());
}

@Override
public void prepare() throws CoreException {
LifecycleHelper.prepare(getConnection());
LifecycleHelper.prepare(getProducer());
}

@Override
public void stop() {
super.stop();
LifecycleHelper.stop(getConnection());
LifecycleHelper.stop(getProducer());
}

@Override
public void close() {
super.close();
LifecycleHelper.close(getConnection());
LifecycleHelper.close(getProducer());
}

/**
* Split the JdbcResult into possibly multiple output messages. Each ResultSet will start in a new message if there are multiple.
*/
@Override
public long translateResult(JdbcResult source, AdaptrisMessage inputMessage) throws SQLException, ServiceException {
long resultSetCount = 0;
try {
// Handle each ResultSet separately and create as many messages as required
for(JdbcResultSet rs: source.getResultSets()) {

// While we still have rows in this ResultSet
final Iterator<JdbcResultRow> rows = rs.getRows().iterator();
while(rows.hasNext()) {
AdaptrisMessage outputMessage = newMessage(inputMessage);

// Fill the message with the requisite number of rows if this ResultSet has enough of them
// We stick this in a try-catch-resources simply to get coverage...
// LimitedResultSet#close() should be doing nothing, since it's only purpose
// is limit the number of iterations you make.
try (LimitedResultSet lrs = new LimitedResultSet(rows, maxRowsPerMessage())) {
DocumentWrapper wrapper = toDocument(outputMessage, lrs);
XmlHelper.writeXmlDocument(wrapper.document, outputMessage, getOutputMessageEncoding(), createTransformer(wrapper));
// Use the configured producer to send the message on its way
getProducer().produce(outputMessage);
resultSetCount += wrapper.resultSetCount;
}
}
}
} catch (Exception e) {
throw ExceptionHelper.wrapServiceException("Failed to process message", e);
}
return resultSetCount;
}

private AdaptrisMessage newMessage(AdaptrisMessage original) {
AdaptrisMessageFactory factory =
ObjectUtils.defaultIfNull(getMessageFactory(), original.getFactory());
AdaptrisMessage result = factory.newMessage();
if (copyMetadata()) {
result.setMetadata(original.getMetadata());
}
result.addMetadata(CoreConstants.PARENT_UNIQUE_ID_KEY, original.getUniqueId());
return result;
}

private DocumentWrapper toDocument(AdaptrisMessage msg, JdbcResultSet rSet) throws Exception {
DocumentWrapper wrapper = createWrapper(msg);
Document doc = wrapper.document;
ColumnStyle elementNameStyle = getColumnNameStyle();

Element results = doc.createElement(elementNameStyle.format(ELEMENT_NAME_RESULTS));
doc.appendChild(results);

List<Element> elements = createListFromResultSet(wrapper, rSet);
for (Element element : elements) {
results.appendChild(element);
}
wrapper.resultSetCount += elements.size();
return wrapper;
}

public Integer getMaxRowsPerMessage() {
return maxRowsPerMessage;
}

public void setMaxRowsPerMessage(Integer maxRowsPerMessage) {
this.maxRowsPerMessage = maxRowsPerMessage;
}

private int maxRowsPerMessage() {
return NumberUtils.toIntDefaultIfNull(getMaxRowsPerMessage(), 1000);
}

/**
* <p>
* Sets the <code>AdaptrisConnection</code> to use for producing split
* messages.
* </p>
*
* @param conn the <code>AdaptrisConnection</code> to use for producing split
*          messages, may not be null
*/
public void setConnection(AdaptrisConnection conn) {
connection = Args.notNull(conn, "connection");
}

/**
* <p>
* Returns the <code>AdaptrisConnection</code> to use for producing split
* messages.
* </p>
*
* @return the <code>AdaptrisConnection</code> to use for producing split
*         messages
*/
public AdaptrisConnection getConnection() {
return connection;
}

/**
* <p>
* Sets the <code>AdaptrisMessageProducer</code> to use for producing split
* messages.
* </p>
*
* @param prod the <code>AdaptrisMessageProducer</code> to use for producing
*          split messages, may not be null
*/
public void setProducer(AdaptrisMessageProducer prod) {
producer = Args.notNull(prod, "producer");

}

/**
* <p>
* Returns the <code>AdaptrisMessageProducer</code> to use for producing split
* messages.
* </p>
*
* @return the <code>AdaptrisMessageProducer</code> to use for producing split
*         messages
*/
public AdaptrisMessageProducer getProducer() {
return producer;
}

public AdaptrisMessageFactory getMessageFactory() {
return messageFactory;
}

public void setMessageFactory(AdaptrisMessageFactory messageFactory) {
this.messageFactory = messageFactory;
}

/**
* Whether to copy metadata from the original message to the split messages.
* <p>
* Note that object metadata is never copied since the nested action is to produce the message
* somewhere.
* </p>
*
* @param b whether to copy metadata from the original message to the split messages (default
*        true)
*/
public void setCopyMetadata(Boolean b) {
copyMetadata = b;
}

public Boolean getCopyMetadata() {
return copyMetadata;
}

private boolean copyMetadata() {
// False for backwards compatible behaviour.
return BooleanUtils.toBooleanDefaultIfNull(getCopyMetadata(), false);
}

public SplittingXmlPayloadTranslator withCopyMetadata(Boolean b) {
setCopyMetadata(b);
return this;
}

public SplittingXmlPayloadTranslator withProducer(AdaptrisMessageProducer b) {
setProducer(b);
return this;
}

public SplittingXmlPayloadTranslator withConnection(AdaptrisConnection b) {
setConnection(b);
return this;
}

public SplittingXmlPayloadTranslator withMaxRowsPerMessage(Integer b) {
setMaxRowsPerMessage(b);
return this;
}

public SplittingXmlPayloadTranslator withMessageFactory(AdaptrisMessageFactory b) {
setMessageFactory(b);
return this;
}


/**
* Wrap a JdbcResultSet to limit the number of rows it will return.
*/
private static class LimitedResultSet implements JdbcResultSet {
private final Iterator<JdbcResultRow> rows;

public LimitedResultSet(Iterator<JdbcResultRow> rows, int rowLimit) {
this.rows = new LimitedIterator<JdbcResultRow>(rows, rowLimit);
}

@Override
public Iterable<JdbcResultRow> getRows() {
return new Iterable<JdbcResultRow>() {
@Override
public Iterator<JdbcResultRow> iterator() {
return rows;
}
};
}

@Override
public void close() { }
}

/**
* Wraps an Iterator and limits the number of objects produced by it
*
* @param <T>
*/
private static class LimitedIterator<T> implements Iterator<T> {
private final Iterator<? extends T> delegate;
private final int limit;
private int objectsProduced;

public LimitedIterator(Iterator<? extends T> delegate, int limit) {
this.delegate = delegate;
this.limit = limit;
}

@Override
public boolean hasNext() {
return objectsProduced < limit && delegate.hasNext();
}

@Override
public T next() {
if(objectsProduced >= limit) {
throw new NoSuchElementException();
}

final T tmp = delegate.next();
objectsProduced++;
return tmp;
}

}

}
