/*
 * $Id: RootHandler.java,v 1.6 2006/06/12 07:50:12 lchan Exp $
 */
package com.adaptris.transform.ff;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.io.*;

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
    long start = System.currentTimeMillis();

    logP.debug("Compiling flat file parser...");

    String enc = getAttribute(root, "encoding");
    if(enc != null && enc.trim().length() > 0)
    {
      encoding = enc;
    }

    //DEBUG=true;
    NodeList segs = root.getChildNodes();

    for ( int i=0; i<segs.getLength(); i++ )
    {
      if ( segs.item(i).getNodeType() == Node.ELEMENT_NODE )
      {
        segments.add(new SegmentHandler(segs.item(i)));
      }
    }

    logP.debug("Compilation took " + (System.currentTimeMillis() - start) + " milliseconds");
  }

  /** process - reads in a text message and parses it
    * @param message - the input message
    * @param output  - StringBuffer to contain the eventual output format
    * @return String containing the input message less that parsed by this segment (usually empty)
    */

  public String process(String message, PrintWriter output)
  {
    long start = System.currentTimeMillis();
    logP.info("Beginning to parse document...");

    output.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>");
    output.print("<root>");

    StreamParser sp = new StreamParser(message);

    for (int i=0; i<segments.size(); i++)
    {
      SegmentHandler seg = (SegmentHandler)segments.get(i);

      if (seg.isThisHandler(sp, false))
      {
        debug("Processing next segment..." + seg.name);

        if ( seg.getCount() == 0 )
        {
          seg.setCount(9999);
        }

        for (int ix=0; ix<seg.getCount(); ix++)
        {
          debug("Before checking handler");
          if (seg.isThisHandler(sp, false)==true)
          {
            seg.process(sp, output);
            //debug("Remainder: " + sp);
          }
          else
          {
            debug("Not this segment " + seg.name);
            break;
          }
        }
      }
    }

    output.print("</root>");

    logP.info("Parsing took " +(System.currentTimeMillis() - start) + " milliseconds");

    return message;
  }

  /** process - reads in a Reader and parses it
    * @param message - the input reader
    * @param output  - StringBuffer to contain the eventual output format
    */

  public void process(Reader message, PrintWriter output)
  {
    long start = System.currentTimeMillis();
    logP.info("Beginning to parse document...");

    output.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>");
    output.print("<root>");

    StreamParser sp = new StreamParser(message);

    for (int i=0; i<segments.size(); i++)
    {
      SegmentHandler seg = (SegmentHandler)segments.get(i);

      if (seg.isThisHandler(sp, false))
      {
        debug("Processing next segment..." + seg.name);

        if ( seg.getCount() == 0 )
        {
          seg.setCount(9999);
        }

        for (int ix=0; ix<seg.getCount(); ix++)
        {
          debug("Before checking handler");
          if (seg.isThisHandler(sp, false)==true)
          {
            seg.process(sp, output);
            //debug("Remainder: " + sp);
          }
          else
          {
            debug("Not this segment " + seg.name);
            break;
          }
        }
      }
    }

    output.print("</root>");
    output.flush();
    output.close();
    logP.info("Parsing took " + (System.currentTimeMillis() - start) + " milliseconds");

    return;
}

  /** Dummy methods */

  @Override
  public void process(StreamParser sp, PrintWriter sb)
  {
    return;
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