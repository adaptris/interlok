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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.io.PrintWriter;

/**
 *
 * @author sellidge
 */
public class SegmentHandler extends Handler
{
  String name;
  ArrayList<Handler> elements = new ArrayList<Handler>();

  /** Constructor - creates parser for this segment using document descriptor
    * @param message - the descriptor node
    */

  SegmentHandler(Node message)
  {
    //DEBUG=true;
    name = getAttribute(message, "name");

    setCount(getIntAttribute(message, "repetitions"));

    NodeList children = message.getChildNodes();

    for ( int i=0; i < children.getLength(); i++ )
    {
      if ( children.item(i).getNodeName().equals("segment") )
      {
        elements.add(new SegmentHandler(children.item(i)));
      }
      else if ( children.item(i).getNodeName().equals("record") )
      {
        elements.add(new RecordHandler(children.item(i)));
      }
    }
  }

  /** process - reads in a text message and parses this segment from it
    * @param sp      - the input StreamParser
    * @param output  - StringBuffer to contain the eventual output format
    */

  @Override
  public void process(StreamParser sp, PrintWriter output)
  {
    //TM.reset();
    //TM.start();

    logP.debug("Begining to process segment " + name);

    StringBuffer content = new StringBuffer();

    output.print("<segment_" + name + ">");

    for ( int i=0; i<elements.size(); i++ )
    {
      Handler h = (Handler)elements.get(i);

      if ( h.getCount() == 0 )
      {
        h.setCount(9999);
      }

      for (int ix=0; ix<h.getCount(); ix++)
      {
        debug("Checking next handler");
        if (h.isThisHandler(sp, false)==true)
        {
          debug("It's this handler [" + sp.getContent() + "]");
          h.process(sp, output);
        }
        else
        {
          debug("It's not this handler [" + sp.getContent() + "]");
          break;
        }
      }
    }

    output.print("</segment_" + name + ">");
    debug(content.toString());

    //TM.stop();
    //logP.debug("Segment " + name + " took " + TM.getDuration() + " milliseconds to process");

    //return message;
  }

  /** isThisHandler - checks to see if next record to be parsed is contained in this
    *                 segment.
    * @param sp     - the StreamParser to be read from
    * @param rewind - not used
    * @return true / false
    */

  @Override
  public boolean isThisHandler(StreamParser sp, boolean rewind)
  {
    boolean retVal = false;

    for ( int i=0; i<elements.size(); i++ )
    {
      Handler h = (Handler)elements.get(i);

      if (h.isThisHandler(sp, true))
      {
        retVal = true;
      }
      else if (h.isOptional() == false)
      {
        // If a mandatory field is missing then it can't be this Handler
        logR.debug("Returning " + h.isOptional() + " from isThisHandler()");
        return false;
      }


      /*

      //This section doesn't work - we continually check the first section
      //of a segment. Would require a drastic rework to be able to check the
      //whole segment.

      if(retVal == false)
      {
        // We have not yet confirmed that we have any records to match
        if (h.isThisHandler(sp, true))
        {
          retVal = true;
        }
        else if (h.isOptional() == false)
        {
          // If a mandatory field is missing then it can't be this Handler
          return false;
        }
      }
      else if (h.isOptional() == false)
      {
        // retVal is already true, we only need to check mandatory fields
        if (h.isThisHandler(sp, true) == false)
        {
          // If a mandatory field is missing then it can't be this Handler
          return false;
        }
      }
      */

    }

    return retVal;
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
}
