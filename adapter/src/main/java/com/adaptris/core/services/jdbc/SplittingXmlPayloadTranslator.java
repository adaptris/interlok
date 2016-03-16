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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Translate the ResultSet contents into an XML Payload.
 * <p>
 * The original message can be included in the output message in order to allow consolidation of XML data and SQL data in a
 * subsequent XSLT step.
 * 
 * The format of the returned message is as follows:
 * 
 * <pre>
 * {@code
 * <Results>
 *   <OriginalMessage>
 *   ...
 *   </OriginalMessage>
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
 * 
 * @config jdbc-xml-payload-translator
 * 
 * @author lchan
 * 
 */
@XStreamAlias("jdbc-splitting-xml-payload-translator")
@DisplayOrder(order = {"maxRowsPerMessage", "allowMultipleResultsPerMessage",
    "columnNameStyle", "columnTranslators", "mergeImplementation", "outputMessageEncoding", 
    "stripIllegalXmlChars", "xmlColumnPrefix", "xmlColumnRegexp", "cdataColumnRegexp",
    "messageFactory"})
public class SplittingXmlPayloadTranslator extends XmlPayloadTranslatorImpl implements SplittingResultSetTranslator {
  private AdaptrisMessageFactory messageFactory;
  private int maxRowsPerMessage;

  public SplittingXmlPayloadTranslator() {
    super();
    setMessageFactory(AdaptrisMessageFactory.getDefaultInstance());
  }

  /**
   * This method cannot be used with this translator. Wouldn't make sense anyway since the whole point
   * of this one is to be able to handle arbitrarily large result sets and the contract for this method
   * requires that the whole ResultSet will be put in this one message.
   */
  @Override
  public void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Iterable<AdaptrisMessage> translate(final JdbcResult source) throws SQLException, ServiceException {
    return new Iterable<AdaptrisMessage>() {
      @Override
      public Iterator<AdaptrisMessage> iterator() {
        try {
          return new Splitter(source);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
  
  private class Splitter implements Iterator<AdaptrisMessage> {
    private final Iterator<JdbcResultSet> rsIt;
    private AdaptrisMessage nextMessage;
    private Iterator<JdbcResultRow> rowIt;
    
    public Splitter(JdbcResult source) throws Exception {
      rsIt = source.getResultSets().iterator();
      nextMessage = buildNextMessage();
    }

    @Override
    public boolean hasNext() {
      return nextMessage != null;
    }

    @Override
    public AdaptrisMessage next() {
      try {
        AdaptrisMessage tmp = nextMessage;
        nextMessage = buildNextMessage();
        return tmp;
      } catch(Exception e) {
        throw new RuntimeException("Unable to construct next message", e);
      }
    }
    
    private AdaptrisMessage buildNextMessage() throws Exception {
      AdaptrisMessage msg = getMessageFactory().newMessage();

      // If we are not currently iterating a ResultSet, begin the next ResultSet if there is one.
      if(rowIt == null) {
        if(rsIt.hasNext()) {
          rowIt = rsIt.next().getRows().iterator();
        } else {
          return null;
        }
      }
      
      // Fill the message with the requisite number of rows if this ResultSet has enough of them
      Document doc = toDocument(msg, rowIt, getMaxRowsPerMessage());
      writeXmlDocument(doc, msg);
      
      // If there are no more rows in this result set, set up for the next one
      // The next call to this method will start the next result set.
      if(!rowIt.hasNext()) {
        rowIt = null;
      }
      
      return msg;
    }
    
  }

  private Document toDocument(AdaptrisMessage msg, Iterator<JdbcResultRow> rows, int maxRows) throws ParserConfigurationException, SQLException {
    DocumentBuilderFactoryBuilder factoryBuilder = documentFactoryBuilder(msg);
    DocumentBuilderFactory factory = factoryBuilder.configure(DocumentBuilderFactory.newInstance());
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    ColumnStyle elementNameStyle = getColumnNameStyle();

    Element results = doc.createElement(elementNameStyle.format(ELEMENT_NAME_RESULTS));
    doc.appendChild(results);

    appendRowsToElement(builder, results, rows, maxRows);
   
    return doc;
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

  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  public void setMessageFactory(AdaptrisMessageFactory messageFactory) {
    this.messageFactory = messageFactory;
  }

}
