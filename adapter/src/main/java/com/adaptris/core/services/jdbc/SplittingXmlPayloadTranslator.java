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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Translate the ResultSet contents into some number of XML messages.
 * 
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
@XStreamAlias("jdbc-splitting-xml-payload-translator")
@DisplayOrder(order = {"maxRowsPerMessage", "producer",
    "columnNameStyle", "columnTranslators", "mergeImplementation", "outputMessageEncoding", 
    "stripIllegalXmlChars", "xmlColumnPrefix", "xmlColumnRegexp", "cdataColumnRegexp",
    "messageFactory"})
public class SplittingXmlPayloadTranslator extends XmlPayloadTranslatorImpl {
  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisConnection connection;
  
  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisMessageProducer producer;

  @AdvancedConfig
  private AdaptrisMessageFactory messageFactory;
  
  private int maxRowsPerMessage = 1000;

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
    getConnection().prepare();
    getProducer().prepare();
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
    final AdaptrisMessageFactory factory = getMessageFactory() == null ? inputMessage.getFactory() : getMessageFactory();
    long resultSetCount = 0;
    try {
      // Handle each ResultSet separately and create as many messages as required
      for(JdbcResultSet rs: source.getResultSets()) {
  
        // While we still have rows in this ResultSet
        final Iterator<JdbcResultRow> rows = rs.getRows().iterator();
        while(rows.hasNext()) {
          AdaptrisMessage outputMessage = factory.newMessage();
          
          // Fill the message with the requisite number of rows if this ResultSet has enough of them
          DocumentWrapper doc = toDocument(outputMessage, new LimitedResultSet(rows, getMaxRowsPerMessage()));
          writeXmlDocument(doc.document, outputMessage);
          
          // Use the configured producer to send the message on its way
          getProducer().produce(outputMessage);
          resultSetCount += doc.resultSetCount;
        }
      }
    } catch (ParserConfigurationException e) {
      throw new ServiceException(e);
    } catch (ProduceException e) {
      throw new ServiceException("Failed to send output message", e);
    } catch (Exception e) {
      throw new ServiceException("Failed to process message", e);
    }
    return resultSetCount;
  }

  private DocumentWrapper toDocument(AdaptrisMessage msg, JdbcResultSet rSet) throws ParserConfigurationException, SQLException {
    DocumentBuilderFactoryBuilder factoryBuilder = documentFactoryBuilder(msg);
    DocumentBuilderFactory factory = factoryBuilder.configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    DocumentWrapper result = new DocumentWrapper(doc, 0);
    ColumnStyle elementNameStyle = getColumnNameStyle();

    Element results = doc.createElement(elementNameStyle.format(ELEMENT_NAME_RESULTS));
    doc.appendChild(results);

    List<Element> elements = createListFromResultSet(builder, doc, rSet);
    for (Element element : elements) {
      results.appendChild(element);
    }
    result.resultSetCount += elements.size();
    return result;
  }
  
  private DocumentBuilderFactoryBuilder documentFactoryBuilder(AdaptrisMessage msg) {
    DocumentBuilderFactoryBuilder factoryBuilder =
        (DocumentBuilderFactoryBuilder) msg.getObjectHeaders().get(JdbcDataQueryService.KEY_DOCBUILDER_FAC);
    return factoryBuilder != null ? factoryBuilder : DocumentBuilderFactoryBuilder.newInstance();
  }
  
  public int getMaxRowsPerMessage() {
    return maxRowsPerMessage;
  }

  public void setMaxRowsPerMessage(int maxRowsPerMessage) {
    this.maxRowsPerMessage = maxRowsPerMessage;
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
    if (conn == null) {
      throw new IllegalArgumentException("param is null");
    }
    connection = conn;
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
    if (prod == null) {
      throw new IllegalArgumentException("param is null");
    }
    producer = prod;
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
      return (objectsProduced < limit) && delegate.hasNext();
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
