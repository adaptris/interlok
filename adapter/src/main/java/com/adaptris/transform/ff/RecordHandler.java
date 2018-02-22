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
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sellidge
 */
public class RecordHandler extends Handler
{
  // Holds array of FieldHandlers

  ArrayList<FieldHandler> Fields = new ArrayList<FieldHandler>();

  // Fields describing record structure

  String separator;
  String rec_id;
  String field_sep;

  boolean optional = true;

  int length       = 0;
  int rec_id_start = 0;
  int rec_id_len   = 0;

  /**
   * Constructor - creates a record parser based on file descriptor node
   * 
   * @param input - the input description node
   */

  RecordHandler(Node input)
  {
    //DEBUG=true;
    separator    = getAttribute(input, "separator");

    if ( separator.startsWith("asc") )
    {
      separator = new String(new char[] {(char)Integer.parseInt(separator.substring(3))});
    }

    rec_id       = getAttribute(input, "rec_id");
    field_sep    = getAttribute(input, "field_sep");

    length       = getIntAttribute(input, "length");
    rec_id_start = getIntAttribute(input, "rec_id_start");
    rec_id_len   = getIntAttribute(input, "rec_id_len");

    try
    {
      if(getAttribute(input, "optional").equalsIgnoreCase("false"))
      {
        optional = false;
      }
    }
    catch(Exception e)
    {
      // do nothing - attribute "optional" is optional!!
    }

    setCount(getIntAttribute(input, "repetitions"));

    NodeList children = input.getChildNodes();

    for ( int i=0; i < children.getLength(); i++ )
    {
      if ( children.item(i).getNodeType() == Node.ELEMENT_NODE)
      {
        Fields.add(new FieldHandler(children.item(i)));
      }
    }
  }

  /** process - parses input file according to record descriptor
    * @param sp      - the input StreamParser
    * @param output  - output message StringBuffer
    */


  @Override
  public void process(StreamParser sp, PrintWriter output)
  {
    String record = "";
    log.trace("Beginning to process record {}", rec_id);

    output.print("<record_" + rec_id + ">");

    try
    {
      record = sp.getContent();
      StreamParser fieldParser = new StreamParser(record);

      log.trace("Extracted record : {}", record);

      for ( int i=0; i < Fields.size(); i++ )
      {
        ((FieldHandler)Fields.get(i)).process(fieldParser, output);
      }

    } catch (Exception e) {
      log.warn("Failed to read record {}, separator='{}',length='{}'", rec_id, separator, length, e);
    }

    output.print("</record_" + rec_id + ">");
  }

  /** Checks to see if next record is this one
    * @param sp     - StreamParser to check against
    * @param rewind - Decides whether to leave the record read (if successfull) or return it to the Stream
    * @return   true is next is this record, false otherwise
    */

  @Override
  public boolean isThisHandler(StreamParser sp, boolean rewind)
  {
    String tmp = new String();

    try
    {
      if ( rec_id.length() == 0)
      {
        if ( length > 0 )
        {
          sp.setParseRule(StreamParser.FIXED_LENGTH, length);
        }
        else
        {
          sp.setParseRule(StreamParser.SEPARATED_STRING, separator.charAt(0));
        }

        sp.readElement();

        if (sp.getContent().length() > 0)
        {
          if (rewind)
          {
            sp.rewindElement(sp.getContent());
          }

          return true;
        }
        else
        {
          sp.rewindElement(sp.getContent());
          return false;
        }
      }
      else
      {
        if ( rec_id_len > 0 )
        {
          sp.setParseRule(StreamParser.FIXED_LENGTH, (rec_id_start-1 + rec_id_len));

          sp.readElement();
          tmp = sp.getContent();
          log.trace("Checking fixed record... id='{}', extract='{}'", rec_id, tmp);

          try {
            if ( rec_id.equals(tmp.substring(rec_id_start-1)))
            {
              log.trace("This record {}", tmp);
              sp.rewindElement(tmp);

              if (length > 0)
              {
                sp.setParseRule(StreamParser.FIXED_LENGTH, length);
              }
              else
              {
                sp.setParseRule(StreamParser.SEPARATED_STRING, separator.charAt(0));
              }

              if (! rewind)
              {

                sp.readElement();

                log.trace("Read record is {}", sp.getContent());

              }

              return true;
            }
            else
            {
              sp.rewindElement(tmp);
              return false;
            }
          }  catch (StringIndexOutOfBoundsException se) {
            log.trace("Record not big enough");
            sp.rewindElement(tmp);
            return false;
          }
        }
        else
        {
          sp.setParseRule(StreamParser.QUOTED_STRING, field_sep.charAt(0), '"');

          ArrayList<String> history = new ArrayList<String>();

          for ( int i=0; i<rec_id_start; i++ )
          {
            log.trace("Looping to ID field");

            try {
              sp.readElement();
              history.add(sp.getContent());
            } catch (Exception e) {
              for (int r=0; r<history.size(); r++)
              {
                sp.rewindElement(history.get(r).toString());
              }
              return false;
            }
          }

          log.trace("Checking variable record... id='{}', extract='{}'", rec_id, sp.getContent());

          if (sp.getContent().equals(rec_id))
          {
            for (int i=0; i<history.size(); i++)
            {
              sp.rewindElement(history.get(i).toString());
            }

            if (! rewind)
            {
              sp.setParseRule(StreamParser.SEPARATED_STRING, separator.charAt(0));
              sp.readElement();
            }
            return true;
          }

          for (int i=0; i<history.size(); i++)
          {
            sp.rewindElement(history.get(i).toString());
          }
          return false;
        }
      }
    } catch (Exception e) {
      log.error("Error reading record", e);
      return false;
    }
  }

  /** Sets repetitions value */

  @Override
  public void setCount(int i)
  {
    COUNT = i;
  }

  /** Returns repetitions value */

  @Override
  public int getCount()
  {
    return COUNT;
  }

  @Override
  public boolean isOptional()
  {
    return optional;
  }
}
