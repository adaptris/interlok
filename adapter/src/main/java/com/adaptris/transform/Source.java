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

package com.adaptris.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xml.sax.InputSource;

/**
 * <p>An input to a transformation process may either be the data
 * source that is to be transformed or it may be the rules used to
 * govern the transformation.</p>
 *
 * <p>This class allows an application to encapsulate information
 * about an input to a transformation process in a single object.
 * This may include a <code>url</code> identifier, character stream,
 * byte stream or a SAX input source.</p>
 *
 * <p>Where a <code>url</code> identifier is used to initialise a
 * <code>Source</code>, it may represent a file on a local system
 * or it may point to a <code>url</code> resource on the web.
 * Note, that it is assumed by this class that any <code>url</code>
 * specified will be pointing at text data and that its encoding
 * will be the same as the default encoding of the client system.</p>
 *
 * <p>Some of the constructors and set methods have an additional
 * parameter specifying a <code>url</code> identifier. This is to get
 * around the fact that a parser has no way of resolving a relative
 * <code>url</code> that appears in a document source if a byte
 * stream or a character stream is being used. Consider that a
 * document source contains the following line: </p>
 *
 * <p align="center">
 * &lt;!DOCTYPE books SYSTEM "books.dtd"&gt;
 * </p>
 *
 * <p>The XML specification says that such a file should be found in
 * the same directory as the source document. But we do not have a
 * directory for the source document because it was in memory when
 * parsing started. To get around this, a <code>url</code> may be
 * supplied along with a byte stream or character stream. This
 * <code>url</code> is not used to read the source document, but
 * is used only as a base for resolving any relative <code>url</code>s
 * found in the source document.</p>
 *
 * <p>Note that the most recent set method, will unset the effect
 * of any previous set invocation. In other words repeated set
 * method invocations are not cumulative in their effect.</p>
 *
 * @author   Trevor Vaughan
 * @version  0.1 April 2001
 */
public class Source {

  private Reader charStream;
  private String url;

  // //////////////////////////////////////
  // constructors
  // //////////////////////////////////////

  public Source() {
  }

  /**
   * <p>Creates a new <code>Source</code> using a <code>url</code> string.
   * The input must be fully resolved.</p>
   *
   * <p>It is assumed that the <code>url</code> is pointing at text data and
   * that its encoding is the same as the default encoding of the client
   * system.</p>
   *
   * @param url the fully resolved url.
   */
  public Source(String url) {
    this();
    this.url = url;
  }

  /**
   * Creates a new <code>Source</code> using a character stream.
   *
   * @param charStream the input character stream.
   */
  public Source(Reader charStream) {
    this();
    this.charStream = charStream;
  }

  /**
   * <p>Returns the character stream representation for this
   * <code>Source</code>. If the object has not been initialised
   * with a character stream then <code>null</code> is returned.</p>
   *
   * 
   * @return a reader.
   */
  public Reader getCharStream() {
    return this.charStream;
  }


  public InputSource getInputSource() throws IOException {
    InputSource is = new InputSource();

    if (this.url != null) {
      is.setSystemId(this.url);
      is.setCharacterStream(this.charStream);
      is.setByteStream(this._connectUrl(this.url));
    } else {
      is.setSystemId(this.url);
      is.setCharacterStream(this.charStream);
    }
    return is;
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof Source) {
      Source rhs = (Source) obj;
      return new EqualsBuilder().append(getCharStream(), rhs.getCharStream()).append(url, rhs.url)
          .isEquals();
    }
    return false;
  }
  
  /** 
   *  @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder(11, 17).append(getCharStream()).append(url).toHashCode();
  }

  private InputStream _connectUrl(String url)
      throws IOException, MalformedURLException {
    URL myUrl = new URL(url);
    URLConnection urlConn = myUrl.openConnection();
    InputStream inputStream = urlConn.getInputStream();
    return inputStream;
  }

} // class Source
