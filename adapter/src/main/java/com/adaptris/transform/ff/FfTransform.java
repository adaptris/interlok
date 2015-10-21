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

package com.adaptris.transform.ff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.transform.ProcessorHandle;
import com.adaptris.transform.Source;
import com.adaptris.transform.Target;
import com.adaptris.transform.TransformFramework;

/**
 * <p>Performs transformations of data where the source is a flat file.</p>
 *
 * @author   Trevor Vaughan
 * @version  0.1 April 2001
 */
public class FfTransform extends TransformFramework {
  private static final int BUFSIZE = 1500;
  private DocumentBuilder db;

  protected PrintStream logOut = System.out;
  protected SimpleDateFormat formatter =
    new SimpleDateFormat("':'yyMMdd':'hh.mm.ss':'");


  public FfTransform(ProcessorHandle processorHandle) throws Exception {
    super(processorHandle);
    db = _getDocumentBuilder();
    // db is used to parse rule into its optimised form
  }

  /**
   * <p>
   * Performs the transformation. The method will first check if <code>rule</code> has already been added to the
   * <code>FfTransform</code> object either through a previous invocation of the <code>transform</code> method or through invoking
   * {@link #addRule(Source)}. The following pseudo-code illustrates the logic applied:
   * </p>
   * 
   * <pre>
   * {@code 
   * check if rule has already been added
   *
   * if rule found then
   *    // do nothing as the rule has already been
   *    // optimised and added to the internal list
   * else
   *    optimise the rule
   *    add optimised rule to the object's internal list
   * end if
   *
   * retrieve optimised rule from internal list
   * transform input using this optimised rule
   * }
   * </pre>
   * 
   * <p>
   * In performing the above, the <code>transform</code> method will always attempt to locate a previously optimised rule that is
   * contained within.
   * </p>
   * 
   * <p>
   * If the caller requires that the <code>rule</code> is to be optimised irrespective of whether it is already contained within the
   * object, then it must first be removed using either {@link #removeRule(Source)}, {@link #removeRule(int)} or
   * {@link #removeRules()}.
   * </p>
   * 
   * <p>
   * Note that the <code>FfTransform</code> object must not be reused until the <code>transform</code> method synchronously returns
   * to the caller.
   * </p>
   * 
   * @param in the input source.
   * @param rule the rule for the transformation.
   * @param out the output.
   * 
   * @throws Exception when an error is detected processing the inputs.
   * 
   * @see #addRule(Source)
   * @see #removeRule(Source)
   * @see #indexOfRule(Source)
   * @see #removeRule(int)
   * @see #removeRules()
   * @see #reset()
   */
  @Override
  public void transform(Source in, Source rule, Target out) throws Exception {
    if (in == null || rule == null || out == null) {
      throw new IllegalArgumentException(
        "Missing argument(s) to " + "transform method");
    }

    // First ascertain if the rule has already been added to the object.
    // If it has then use the optimised form, otherwise optimise it and add
    // the rule to the internal list.
    if (!(super.ruleList.containsKey(rule))) {
      this.addRule(rule);
    }

    // Getting to this point, we know we have the optimised version of
    // the rule contained within this object.
    RootHandler optimisedRule = (RootHandler) super.ruleList.getValue(rule);

    // Convert the input to the transform method into a form
    // appropriate for passing to process().
    Reader message = _convertToReader(in);

    // Prepare the output for passing to process().
    // StringBuffer transformedOutput = new StringBuffer(BUFSIZE);


    // ////////////////////////////////////////
    // TRANSFORMATION OF DATA HERE
    // ////////////////////////////////////////
    optimisedRule.process(message, new PrintWriter(out.getWriter()));
    // ////////////////////////////////////////
    // TRANSFORMATION OF DATA HERE
    // ////////////////////////////////////////

  } // method transform

  /**
   * <p>Adds a rule. The rule is automatically processed and stored
   * within <code>FfTransform</code> in an optimised format.
   * This allows the {@link #transform(Source,Source,Target)} method
   * to select and utilise the optimised rule contained within based
   * on a lookup using the non-optimised rule passed for the second
   * argument.</p>
   *
   * @param  rule  the rule to add.
   * @throws Exception  when there is an error optimising the rule.
   * @see    #removeRule(Source)
   */
  @Override
  public void addRule(Source rule) throws Exception {
    if (rule == null) {
      throw new IllegalArgumentException();
    }

    if (log != null && log.isDebugEnabled()) {
      log.debug("addRule() invoked: rule <" + rule + ">");
    }

    super.ruleList.add(rule, new RootHandler(_optimiseRule(db, rule)));
  }

  // ///////////////////////////////////////////////////////////
  // all the non-public stuff here
  // ///////////////////////////////////////////////////////////

  /** Returns a string padded out to a specified length
    * @param input - string to be padded
    * @param len   - length to pad string to
    * @param pad   - character to pad string with
    * @return input string padded to correct length
    */
  protected static String padChar(String input, int len, String pad) {
    String output = new String();

    if (len > 0) {
      if (input.length() > len) {
        output = input.substring(0, len);
      } else {
        output = input;
        for (int i = input.length(); i < len; i++) {
          output += pad;
        }
      }
    } else {
      output = input;
    }

    return output;
  }

  // This functionality has deliberately been left out of
  // the Target class as repreatable reads of the same stream
  // is awkward.
  private Reader _convertToReader(Source in) throws IOException {
    Reader message = null;
    InputStream bs = in.getByteStream();
    Reader cs = in.getCharStream();

    if (bs == null && cs == null) {
      message = new StringReader(in.getString());
    } else if (bs != null) {
      PushbackInputStream pis = new PushbackInputStream(bs, 3);
      //Check for UTF-8 Byte Order Mark sequence
      byte[] bom = new byte[3];
      pis.read(bom);

      if(bom[0] != -17 && bom[1] != -69 && bom[2] != -65) {
        //this is not a UTF-8 stream so unread the three bytes
        pis.unread(bom);
      }

      // We have to assume that the message is in the format set by file.encoding
      message = new InputStreamReader(pis, System.getProperty("file.encoding"));
    } else if (cs != null) {
      message = cs;
    } else {
      // Will get here if the Source object was not initialised
      throw new RuntimeException("FfTransform: input has not been initialised");
    }

    return message;
  }

  private String _readByteStream(InputStream byteStream) throws IOException {
    InputStreamReader i = new InputStreamReader(byteStream);
    BufferedReader in = new BufferedReader(i);

    String result = _read(in);

    // No need to close the stream.
    //in.close();

    return result;
  }

  private String _readCharStream(Reader charStream) throws IOException {
    BufferedReader in = new BufferedReader(charStream);

    String result = _read(in);

    // No need to close the stream.
    //in.close();

    return result;
  }

  private String _read(java.io.BufferedReader in) throws IOException {
    int c;
    StringBuffer buffer = new StringBuffer(BUFSIZE);

    // We are reading a character at a time so not to loose
    // newlines. Quick performance test showed that reading a
    // 300KB file took 180 ms to read.
    while ((c = in.read()) != -1) {
      buffer.append((char) c);
    }

    return buffer.toString();
  }

  private Element _optimiseRule(DocumentBuilder db, Source rule)
    throws SAXException, IOException {
    Document output = db.parse(rule.getInputSource());
    Element optimisedRule = output.getDocumentElement();
    return optimisedRule;
  }

  // The DocumentBuilder returned is used to parse the rule
  // (passed to the transform method) into its optimised format.
  private DocumentBuilder _getDocumentBuilder() throws Exception {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactoryBuilder.newInstance().configure(DocumentBuilderFactory.newInstance());
      return dbf.newDocumentBuilder();
    } catch (Exception e) {
      throw new Exception(e.getMessage());
    }
  }

} // class FfTransform
