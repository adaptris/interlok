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
import java.io.Reader;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sellidge
 */
public class RootHandler extends Handler
{
  ArrayList<Handler> segments = new ArrayList<Handler>();

  private String encoding = "UTF-8";

  /** Constructor - creates parser for this document using document descriptor
    * @param root - the descriptor node
    */

  public RootHandler(Node root)
  {
    String enc = getAttribute(root, "encoding");
    if(enc != null && enc.trim().length() > 0)
    {
      encoding = enc;
    }

    NodeList segs = root.getChildNodes();

    for ( int i=0; i<segs.getLength(); i++ )
    {
      if ( segs.item(i).getNodeType() == Node.ELEMENT_NODE )
      {
        segments.add(new SegmentHandler(segs.item(i)));
      }
    }
  }

  /** process - reads in a text message and parses it
    * @param message - the input message
    * @param output  - StringBuffer to contain the eventual output format
    * @return String containing the input message less that parsed by this segment (usually empty)
    */

  public String process(String message, PrintWriter output)
  {
    output.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>");
    output.print("<root>");

    StreamParser sp = new StreamParser(message);

    for (int i=0; i<segments.size(); i++)
    {
      SegmentHandler seg = (SegmentHandler)segments.get(i);

      if (seg.isThisHandler(sp, false))
      {
        log.trace("Processing next segment... {}", seg.name);

        if ( seg.getCount() == 0 )
        {
          seg.setCount(9999);
        }

        for (int ix=0; ix<seg.getCount(); ix++)
        {
          if (seg.isThisHandler(sp, false)==true)
          {
            seg.process(sp, output);
          }
          else
          {
            log.trace("Not this segment {}", seg.name);
            break;
          }
        }
      }
    }

    output.print("</root>");
    return message;
  }

  /** process - reads in a Reader and parses it
    * @param message - the input reader
    * @param output  - StringBuffer to contain the eventual output format
    */

  public void process(Reader message, PrintWriter output)
  {
    output.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>");
    output.print("<root>");

    StreamParser sp = new StreamParser(message);

    for (int i=0; i<segments.size(); i++)
    {
      SegmentHandler seg = (SegmentHandler)segments.get(i);

      if (seg.isThisHandler(sp, false))
      {
        log.trace("Processing next segment...{}", seg.name);

        if ( seg.getCount() == 0 )
        {
          seg.setCount(9999);
        }

        for (int ix=0; ix<seg.getCount(); ix++)
        {
          if (seg.isThisHandler(sp, false)==true)
          {
            seg.process(sp, output);
          }
          else
          {
            log.trace("Not this segment {}", seg.name);
            break;
          }
        }
      }
    }

    output.print("</root>");
    output.flush();
    output.close();
}

  /** Dummy methods */

  @Override
  public void process(StreamParser sp, PrintWriter sb)
  {
  }

  @Override
  public boolean isThisHandler(StreamParser tmp, boolean dummy)
  {
    return true;
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
