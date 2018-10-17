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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
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
  private DocumentBuilder db;

  protected SimpleDateFormat formatter =
    new SimpleDateFormat("':'yyMMdd':'hh.mm.ss':'");


  public FfTransform() throws Exception {
    super();
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
   */
  @Override
  public void transform(Source in, Source rule, Target out) throws Exception {
    Args.notNull(in, "source");
    Args.notNull(rule, "rule");
    Args.notNull(out, "target");

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
    try (Reader message = _convertToReader(in); PrintWriter p = new PrintWriter(out.getWriter())) {
      optimisedRule.process(message, p);
    }

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
    Args.notNull(rule, "rule");
    if (log != null && log.isDebugEnabled()) {
      log.debug("addRule() invoked: rule <" + rule + ">");
    }

    super.ruleList.add(rule, new RootHandler(_optimiseRule(db, rule)));
  }

  // This functionality has deliberately been left out of
  // the Target class as repreatable reads of the same stream
  // is awkward.
  private Reader _convertToReader(Source in) throws IOException {
    if (in.getCharStream() != null) {
      return in.getCharStream();
    }
    throw new RuntimeException("FfTransform: input has not been initialised");
  }

  private Element _optimiseRule(DocumentBuilder db, Source rule)
      throws SAXException, IOException, URISyntaxException {
    Document output = db.parse(rule.getInputSource());
    Element optimisedRule = output.getDocumentElement();
    return optimisedRule;
  }

  // The DocumentBuilder returned is used to parse the rule
  // (passed to the transform method) into its optimised format.
  private DocumentBuilder _getDocumentBuilder() throws ParserConfigurationException {
      return DocumentBuilderFactoryBuilder.newInstance().withNamespaceAware(true)
          .newDocumentBuilder(DocumentBuilderFactory.newInstance());
  }

} // class FfTransform
