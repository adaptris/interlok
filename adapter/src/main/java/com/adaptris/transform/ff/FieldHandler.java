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

import java.io.PrintWriter;

import org.w3c.dom.Node;

/**
 *
 * @author sellidge
 */
public class FieldHandler extends Handler
{
  // Fields which define the field

  private String name;
  private String separator;
  private int    length;

  //new option added to enable override of default quoted string type
  private boolean useQuotedString = true;

  // Fields which contain the field value

  private String content;

  /** Constructor - receives the descriptor node and uses it's details
    * to populate the fields which govern field parsing.
    * @param node - the input node
    */

  public FieldHandler(Node node)
  {
    //DEBUG = true;
    name      = getAttribute(node, "name");
    separator = getAttribute(node, "separator");
    length    = getIntAttribute(node, "length");

    String tmp = getAttribute(node, "use_quoted_string");
    if(tmp.equals("false")) {
      useQuotedString = false;
    }

    if ( separator.startsWith("asc") )
    {
      separator = new String(new char[] {(char)Integer.parseInt(separator.substring(3))});
    }
  }

  /** getName - returns name of field
    * @return String representation of name of field
    */

  public String getName()
  {
    return name;
  }

  /** process - function which receives a record and reads the next field
    * from it according to the definition read from the message descriptor.
    * @param inputRecord - the StreamParser to read the record from
    * @param output      - the StringBuffer to receive the output
    */

  @Override
  public void process(StreamParser inputRecord, PrintWriter output)
  {
    String record = "";

    try
    {
      log.trace("Reading field {}...", getName());

      if (length > 0)
      {
        log.trace("Record is fixed length... {}", length);
        inputRecord.setParseRule(StreamParser.FIXED_LENGTH, length);
        inputRecord.readElement();
        record  = inputRecord.getContent();
      }
      else
      {
        log.trace("Field is {} separated", separator);

        //Added a check to see if this field should be a simple separated string
        if(useQuotedString == true) {
          inputRecord.setParseRule(StreamParser.QUOTED_STRING, separator.charAt(0), '"');
        } else {
          inputRecord.setParseRule(StreamParser.SEPARATED_STRING, separator.charAt(0));
        }

        inputRecord.readElement();
        record  = inputRecord.getContent();
      }

      content = inputRecord.getContent().trim();
    }
    catch (Exception e)
    {
      log.warn("Failed to read field {}, separator='{}',length='{}'", getName(), separator, length);
    }

    int ix = -1;

    while ((ix = content.indexOf("&", ix+1)) >= 0)
    {
      content = content.substring(0,ix) + "&amp;" + content.substring(ix+1);
    }

    while ((ix = content.indexOf("<")) >= 0)
    {
      content = content.substring(0,ix) + "&lt;" + content.substring(ix+1);
    }

    while ((ix = content.indexOf(">")) >= 0)
    {
      content = content.substring(0,ix) + "&gt;" + content.substring(ix+1);
    }

    while ((ix = content.indexOf("'")) >= 0)
    {
      content = content.substring(0,ix) + "&apos;" + content.substring(ix+1);
    }

    while ((ix = content.indexOf('"')) >= 0)
    {
      content = content.substring(0,ix) + "&quot;" + content.substring(ix+1);
    }

    log.trace("Read in field {}, contents: {}", getName(), getContent());

    output.print("<" + getName() + ">" + getContent() + "</" + getName() + ">");
  }

  /** Dummy function */

  @Override
  public void setCount(int i)
  {
    COUNT = i;
    return;
  }

  /** Dummy function */

  @Override
  public int getCount()
  {
    return COUNT;
  }

  @Override
  public boolean isThisHandler(StreamParser input, boolean dummy)
  {
    return true;
  }

  /** Function to return content of field after reading
    * @return String contents of field
    */

  public String getContent()
  {
    return content;
  }
}
