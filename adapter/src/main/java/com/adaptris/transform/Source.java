package com.adaptris.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.transform.stream.StreamSource;

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

  // //////////////////////////////////////
  // internal state
  // //////////////////////////////////////
  private String url;
  private Reader charStream;
  private InputStream byteStream;

  // //////////////////////////////////////
  // constructors
  // //////////////////////////////////////

  /**
   * <p>Zero-argument default constructor.</p>
   *
   * @see #setUrl(String)
   * @see #setFile(File)
   * @see #setCharStream(Reader)
   * @see #setCharStream(Reader,String)
   * @see #setByteStream(InputStream)
   * @see #setByteStream(InputStream,String)
   * @see #setInputSource(InputSource)
   */
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
   * @see   #setUrl(String)
   * @see   #getUrl()
   * @see   #getString()
   */
  public Source(String url) {
    this();
    _setUrl(url);
  }

  /**
   * <p>Creates a new <code>Source</code> using a <code>File</code>.
   * The <code>File</code> input is internally resolved as a
   * <code>url</code> string.</p>
   *
   * @param  file       the input <code>File</code>.
   * @throws MalformedURLException if the path cannot be parsed as a 
   * <code>url</code> string.
   * @see   #setFile(File)
   * @see   #getUrl()
   * @see   #getString()
   */
  @SuppressWarnings("deprecation")  
  public Source(File file) throws MalformedURLException {
    this();
    _setUrl(file.toURL().toString());
  }

  /**
   * <p>Creates a new <code>Source</code> using a character stream.
   * This is the same as invoking {@link #Source(Reader,String)}
   * and passing <code>null</code> for the second parameter.</p>
   *
   * @param charStream the input character stream.
   * @see   #Source(Reader,String)
   * @see   #setCharStream(Reader)
   * @see   #setCharStream(Reader,String)
   * @see   #getCharStream()
   */
  public Source(Reader charStream) {
    this();
    _setCharStream(charStream);
  }

  /**
   * <p>Creates a new <code>Source</code> using a character stream.
   * The <code>url</code> identifier is optional but it is still useful
   * to provide one since the application can use it to resolve
   * relative <code>url</code>s within the document source.</p>
   *
   * @param charStream  the input character stream.
   * @param url         the url as a string.
   * @see   #Source(Reader)
   * @see   #setCharStream(Reader)
   * @see   #setCharStream(Reader,String)
   * @see   #getCharStream()
   */
  public Source(Reader charStream, String url) {
    this();
    _setCharStream(charStream);
    _setUrl(url);
  }

  /**
   * <p>Creates a new <code>Source</code> using a byte stream.
   * This is the same as invoking {@link #Source(InputStream,String)}
   * and passing <code>null</code> for the second parameter.</p>
   *
   * @param byteStream the input byte stream.
   * @see   #Source(InputStream,String)
   * @see   #setByteStream(InputStream)
   * @see   #setByteStream(InputStream,String)
   * @see   #getByteStream()
   */
  public Source(InputStream byteStream) {
    this();
    _setByteStream(byteStream);
  }

  /**
   * <p>Creates a new <code>Source</code> using a byte stream.
   * The <code>url</code> identifier is optional but it is still useful
   * to provide one since the application can use it to resolve
   * relative <code>url</code>s within the document source.</p>
   *
   * @param byteStream  the input byte stream.
   * @param url         the url as a string.
   * @see   #Source(InputStream)
   * @see   #setByteStream(InputStream)
   * @see   #setByteStream(InputStream,String)
   * @see   #getByteStream()
   */
  public Source(InputStream byteStream, String url) {
    this();
    _setByteStream(byteStream);
    _setUrl(url);
  }

  /**
   * <p>Creates a new <code>Source</code> using a SAX input source.</p>
   *
   * @param inputSource  the SAX input source.
   * @see   #setInputSource(InputSource)
   * @see   #getString()
   */
  public Source(InputSource inputSource) {
    _setInputSource(inputSource);
  }

  // //////////////////////////////////////
  //  set methods
  // //////////////////////////////////////

  /**
   * <p>Sets the <code>Source</code> using a <code>url</code> string.
   * The input must be fully resolved.</p>
   *
   * @param url the fully resolved url.
   * @see   #Source(String)
   * @see   #getUrl()
   * @see   #getString()
   */
  public void setUrl(String url) {
    _setCharStream(null);
    _setByteStream(null);
    _setUrl(url);
  }

  /**
   * <p>Sets the <code>Source</code> using a <code>File</code>.
   * The <code>File</code> input is internally resolved as a
   * <code>url</code> string.</p>
   *
   * @param  file       the input <code>File</code>.
   * @throws Exception if the path cannot be parsed as a <code>url
   * </code> string.
   * @see   #Source(File)
   * @see   #getUrl()
   * @see   #getString()
   */
  @SuppressWarnings("deprecation")  
  public void setFile(File file) throws Exception {
    _setCharStream(null);
    _setByteStream(null);
    _setUrl(file.toURL().toString());
  }

  /**
   * <p>Sets the <code>Source</code> using a character stream.
   * This is the same as invoking {@link #setCharStream(Reader,String)}
   * and passing <code>null</code> for the second parameter.</p>
   *
   * @param charStream the input character stream.
   * @see   #setCharStream(Reader,String)
   * @see   #Source(Reader)
   * @see   #Source(Reader,String)
   * @see   #getCharStream()
   */
  public void setCharStream(Reader charStream) {
    _setUrl(null);
    _setByteStream(null);
    _setCharStream(charStream);
  }

  /**
   * <p>Sets the <code>Source</code> using a character stream.
   * The <code>url</code> identifier is optional but it is still useful
   * to provide one since the application can use it to resolve
   * relative <code>url</code>s within the document source.</p>
   *
   * @param charStream  the input character stream.
   * @param url         the url as a string.
   * @see   #setCharStream(Reader)
   * @see   #Source(Reader)
   * @see   #Source(Reader,String)
   * @see   #getCharStream()
   */
  public void setCharStream(Reader charStream, String url) {
    _setByteStream(null);
    _setUrl(url);
    _setCharStream(charStream);
  }

  /**
   * <p>Sets the <code>Source</code> using a byte stream.
   * This is the same as invoking {@link #setByteStream(InputStream,String)}
   * and passing <code>null</code> for the second parameter.</p>
   *
   * @param byteStream the input byte stream.
   * @see   #setByteStream(InputStream,String)
   * @see   #Source(InputStream)
   * @see   #Source(InputStream,String)
   * @see   #getByteStream()
   */
  public void setByteStream(InputStream byteStream) {
    _setUrl(null);
    _setCharStream(null);
    _setByteStream(byteStream);
  }

  /**
   * <p>Sets the <code>Source</code> using a byte stream.
   * The <code>url</code> identifier is optional but it is still useful
   * to provide one since the application can use it to resolve
   * relative <code>uri</code>s within the document source.</p>
   *
   * @param byteStream  the input byte stream.
   * @param url         the url as a string.
   * @see   #setByteStream(InputStream)
   * @see   #Source(InputStream)
   * @see   #Source(InputStream,String)
   * @see   #getByteStream()
   */
  public void setByteStream(InputStream byteStream, String url) {
    _setCharStream(null);
    _setUrl(url);
    _setByteStream(byteStream);
  }

  /**
   * <p>Sets the <code>Source</code> using a SAX input source.</p>
   *
   * @param inputSource  the SAX input source.
   * @see   #Source(InputSource)
   * @see   #getInputSource()
   * @see   #getString()
   */
  public void setInputSource(InputSource inputSource) {
    _setInputSource(inputSource);
  }

  // //////////////////////////////////////
  //  get methods
  // //////////////////////////////////////

  /**
   * <p>Returns the <code>url</code> for this <code>Source</code>.
   * If the object has not been initialised with a <code>url</code>
   * then <code>null</code> is returned.</p>
   *
   * @see #Source(String)
   * @see #Source(File)
   * @see #Source(InputSource)
   * @see #setUrl(String)
   * @see #setFile(File)
   * @see #setInputSource(InputSource)
   * @return the url.
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * <p>Returns the character stream representation for this
   * <code>Source</code>. If the object has not been initialised
   * with a character stream then <code>null</code> is returned.</p>
   *
   * @see #Source(Reader)
   * @see #Source(Reader,String)
   * @see #Source(InputSource)
   * @see #setCharStream(Reader)
   * @see #setCharStream(Reader,String)
   * @see #setInputSource(InputSource)
   * 
   * @return a reader.
   */
  public Reader getCharStream() {
    return this.charStream;
  }

  /**
   * <p>Returns the byte stream representation for this
   * <code>Source</code>. If the object has not been initialised
   * with a byte stream then <code>null</code> is returned.</p>
   *
   * @see #Source(InputStream)
   * @see #Source(InputStream,String)
   * @see #Source(InputSource)
   * @see #setInputSource(InputSource)
   * @return the bytestream
   */
  public InputStream getByteStream() {
    return this.byteStream;
  }

  /**
   * <p>Returns the SAX input source representation for this
   * <code>Source</code>. This will never return <code>null</code>.</p>
   *
   * @see #setInputSource(InputSource)
   * @return the input source.
   */
  public InputSource getInputSource() {
    return _getInputSource();
  }

  /**
   * <p>Returns the <code>StreamSource</code> representation for
   * this <code>Source</code>. This will never return <code>null</code>.</p>
   * @return the stream source.
   */
  public StreamSource getStreamSource() {
    StreamSource ss;

    if (charStream != null) {
      ss = new StreamSource(charStream);
    } else if (byteStream != null) {
      ss = new StreamSource(byteStream);
    } else {
      try {
        ss = new StreamSource(_connectUrl(url));
      } catch (Exception e) {
        ss = new StreamSource(url);
      }
    }

    return ss;
  }

  /**
   * <p>
   * Returns a string representation of <code>Source</code>. This method <b>only</b> returns a string representation when
   * <code>Source</code> has been initialised by one of the following:
   * </p>
   * 
   * <p>
   * <ul type="square">
   * <li>a <code>url</code> identifier or</li>
   * <li>a <code>File</code> or</li>
   * <li>an <code>InputSource</code> (where <b>only</b> the system identifier has been set)</li>
   * </ul>
   * </p>
   * 
   * <p>
   * otherwise it returns <code>null</code> in all other cases.
   * </p>
   * 
   * <p>
   * The following code snippet llustrates its use:
   * </p>
   * 
   * <pre>
   * {@code 
   * Source mySource = new Source( new File("myFile.txt") );
   *                       .
   *                       .
   *                       .
   * if (mySource.getCharStream() == null && mySource.getByteStream() == null)
   * {
   *    String foo = mySource.getString();
   *    System.out.println(foo);
   * }
   * }
   * </pre>
   * 
   * @throws IOException if an I/O exception occurs.
   * @throws MalformedURLException if a malformed url has been detected.
   * @see #Source(String)
   * @see #Source(File)
   * @see #Source(InputSource)
   * @see #setUrl(String)
   * @see #setFile(File)
   * @see #setInputSource(InputSource)
   * @see #getCharStream()
   * @see #getByteStream()
   * @return the string.
   */
  //DEVNOTE: this functionality is not supplied with streams. To do so would
  //move the current position within the stream. This is not a problem with
  //a url as we open, get data and then close again.
  public String getString() throws IOException, MalformedURLException {
    if (this.url != null
      && this.charStream == null
      && this.byteStream == null) {
      return _readUrl(this.url);
    }

    return null;
  }

  // //////////////////////////////////////
  //  other methods
  // //////////////////////////////////////

  /** @see Object#equals(Object)
   * <p>Returns <code>true</code> if the objects are equal
   * otherwise it returns <code>false</code>.</p>
   */
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj != null && this.getClass() == obj.getClass()) {
      Source s = (Source) obj; // downcast

      if (((this.url != null && this.url.equals(s.url)) || (this.url == s.url))
        && this.charStream == s.charStream
        && this.byteStream == s.byteStream) {
        return true;
      }
    }

    return false;
  }
  
  /** 
   *  @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    int hc = 0;
    hc += (url == null) ? 0 : url.hashCode();
    hc += (charStream == null) ? 0 : charStream.hashCode();
    hc += (byteStream == null) ? 0 : byteStream.hashCode();
    return hc;
  }

  /**
   * <p>Resets the object. The internal state of the object
   * is set to <code>null</code>. This effectively puts the
   * object in the same state as if the no argument
   * constructor {@link #Source()} had been invoked.</p>
   */
  public final void reset() {
    this.url = null;
    this.charStream = null;
    this.byteStream = null;
  }

  /** @see Object#toString()
   * <p>Returns a string representation of the class.</p>
   */
  public String toString() {
    return (
      getClass().getName()
        + ':'
        + this.url
        + ':'
        + this.charStream
        + ':'
        + this.byteStream
        + '@'
        + Integer.toHexString(hashCode()));
  }

  // //////////////////////////////////////
  //  private  methods
  // //////////////////////////////////////

  private void _setUrl(String url) {
    this.url = url;
  }

  private void _setCharStream(Reader charStream) {
    this.charStream = charStream;
  }

  private void _setByteStream(InputStream byteStream) {
    this.byteStream = byteStream;
  }

  private void _setInputSource(InputSource inputSource) {
    this.url = inputSource.getSystemId();
    this.charStream = inputSource.getCharacterStream();
    this.byteStream = inputSource.getByteStream();
  }

  // modified to ensure that we attempt to use the proxy server
  private InputSource _getInputSource() {
    InputSource is = new InputSource();

    if (this.url != null) {
      is.setSystemId(this.url);
      is.setCharacterStream(this.charStream);

      try {
        is.setByteStream(this._connectUrl(this.url));
      } catch (IOException ie) {
        is.setByteStream(this.byteStream);
      }
    } else {
      is.setSystemId(this.url);
      is.setCharacterStream(this.charStream);
      is.setByteStream(this.byteStream);
    }

    return is;
  }

  // This opens the url, reads all its data and returns the data
  // as a string. This code assumes that the url is pointing at
  // text data and that its encoding is the same as the default
  // encoding of the client system.
  private String _readUrl(String url)
    throws IOException, java.net.MalformedURLException {
    InputStream inputStream = _connectUrl(url);

    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

    String result = _read(in);

    in.close();

    return result;

  } // method _readUrl

  // This code was previously part of the _readUrl method, but has been
  // separated out to allow use elsewhere in the class.
  //
  // It now enables the use of a proxy server if required. To enable proxy
  // server usage, add the following line to the startAdapter lax file:
  //
  // lax.nl.java.option.additional -DproxySet=true -DproxyUser=user
  //            -DproxyPass=passwd -DproxyHost=192.168.254.xxx -DproxyPort=xxxx

  private InputStream _connectUrl(String url)
    throws IOException, java.net.MalformedURLException {
    URL myUrl = new URL(url); // throws MalformedURLException
    URLConnection urlConn = myUrl.openConnection();
    // applyBasicProxyAuthorisation(urlConn);
    InputStream inputStream = urlConn.getInputStream();
    return inputStream;
  }

  /*
  Do not read any streams as we cannot reset to the beginning of
  the stream if the stream is subsequently accessed.
    private String _readCharStream(Reader charStream) throws IOException
    {
      BufferedReader in = new BufferedReader(charStream);
  
      String result = _read(in);
  
      // No need to close the stream.
      //in.close();
  
      return result;
    }
   */

  private String _read(BufferedReader in) throws IOException {
    int c;
    StringBuffer buffer = new StringBuffer(1500);

    // We are reading a character at a time so not to loose
    // newlines. Quick performance test showed that reading a
    // 300KB file took 180 ms to read.
    while ((c = in.read()) != -1) {
      buffer.append((char) c);
    }

    return buffer.toString();
  }

} // class Source
