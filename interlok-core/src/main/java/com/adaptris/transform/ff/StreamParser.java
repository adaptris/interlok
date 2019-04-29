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
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * 
 * @author sellidge
 */
public class StreamParser
{
  private PushbackReader  pr = null;
  private StreamTokenizer st = null;

  private int type;
  private int length;
  private char sep;
  private char esc;

  private String content;

  // End of file indicator
  private boolean EOF;

  public static final int FIXED_LENGTH     = 0;
  public static final int ESCAPED_STRING   = 1;
  public static final int QUOTED_STRING    = 2;
  public static final int SEPARATED_STRING = 3;

  private int BUFFER_SIZE = 10000;

  public StreamParser(String string)
  {
    pr = new PushbackReader(new StringReader(string), BUFFER_SIZE);
    st = new StreamTokenizer(pr);
  }

  public StreamParser(Reader reader)
  {
    pr = new PushbackReader(reader, BUFFER_SIZE);
    st = new StreamTokenizer(pr);
  }

  public void setParseRule(int type, int length)
  {
    this.type = type;
    this.length = length;
  }

  public void setParseRule(int type, char sep)
  {
    this.type = type;
    this.sep  = sep;
    this.esc  = '0';

    st.resetSyntax();
    st.eolIsSignificant(false);

    st.wordChars(0, sep-1);
    st.wordChars(sep+1, 255);
  }

  public void setParseRule(int type, char sep, char esc)
  {
    if ( this.type != type || this.sep != sep || this.esc != esc)
    {
      this.type = type;
      this.sep = sep;
      this.esc = esc;

      st.resetSyntax();
      st.eolIsSignificant(false);

      if ( sep < esc )
      {
        st.wordChars(0, sep-1);
        st.wordChars(esc+1, 255);

        if (esc-sep > 1 )  st.wordChars(sep+1, esc-1);
      }
      else
      {
        st.wordChars(0, esc-1);
        st.wordChars(sep+1, 255);

        if (sep-esc > 1)  st.wordChars(esc+1, sep-1);
      }
    }
  }

  public void readElement() throws Exception
  {
    switch (type)
    {
      case StreamParser.FIXED_LENGTH     : readFixedElement();     break;
      case StreamParser.ESCAPED_STRING   : readEscapedElement();   break;
      case StreamParser.QUOTED_STRING    : readQuotedElement();    break;
      case StreamParser.SEPARATED_STRING : readSeparatedElement(); break;
      default : throw new Exception("Invalid parse type");
    }
  }

  public String getContent()
  {
    if (EOF)
    {
      // return an empty string if we are at the end of the file
      // avoids the problem of having added a separator character
      // when rewinding on to an empty stream.
      return "";
    }
    else
    {
      return content;
    }
  }

  public void rewindElement(String string) throws IOException
  {

    // If we had previously reached the end of the file, but have
    // some data to rewind, then set EOF back to false.
    if (string.length() > 0)
    {
      EOF = false;
    }

    string = commentString(string);
    char[] chars = string.toCharArray();

    for (int i=0; i<chars.length; i+=BUFFER_SIZE)
    {
      pr.unread(chars, i, (chars.length - i) < BUFFER_SIZE ? (chars.length - i) : BUFFER_SIZE);
    }

  }

  private void readFixedElement() throws IOException
  {
    char[] charArray = new char[length];
    int readCount = pr.read(charArray, 0, length);

    if (readCount == -1) {
      EOF = true;
    } else {
      content = new String(charArray, 0, readCount);
    }
  }

  private void readSeparatedElement() throws IOException
  {
    StringBuffer output = new StringBuffer();

    ContentBuilder:
    while (true)
    {
      st.nextToken();

      switch ( st.ttype )
      {
        case StreamTokenizer.TT_EOF  :
          // If we reach the end of the file and have no data to return,
          // set the EOF flag.
          if(output.length() == 0) EOF = true;
          break ContentBuilder;
        case StreamTokenizer.TT_WORD : output.append(st.sval); break;
        default :
          break ContentBuilder;
      }
      //output.append(st.sval); <-- not sure why this was here, but it seems to be the problem
    }

    content = output.toString();
  }

  private void readQuotedElement() throws IOException
  {
    StringBuffer element  = new StringBuffer();
    boolean quoteLastChar = false;
    boolean inQuotes      = false;

    ContentBuilder:
    while(true)
    {
      st.nextToken();

      switch ( st.ttype )
      {
        case StreamTokenizer.TT_EOF :
          // If we reach the end of the file and have no data to return,
          // set the EOF flag.
          if(element.length() == 0) EOF = true;
          break ContentBuilder;
        case StreamTokenizer.TT_NUMBER : quoteLastChar = false; break;
        case StreamTokenizer.TT_WORD   : quoteLastChar = false; element.append(st.sval); break;
        default :
          if ( (char)st.ttype == esc && quoteLastChar == false )
          {
            if ( inQuotes == false )
            {
              inQuotes = true;
            }
            else
            {
              inQuotes = false;
              quoteLastChar = true;
            }
          }
          else if ( (char)st.ttype == esc && quoteLastChar == true )
          {
            element.append((char)st.ttype);
            quoteLastChar = false;
            inQuotes = true;
          }
          else if ( (char)st.ttype == sep && inQuotes == true )
          {
            element.append((char)st.ttype);
            quoteLastChar = false;
          }
          else if ( (char)st.ttype == sep && inQuotes == false )
          {
            break ContentBuilder;
          }
        ;
      }
    }

    content = element.toString();
  }

  private void readEscapedElement() throws IOException
  {
    StringBuffer element = new StringBuffer();
    boolean escapeLastChar = false;

    ContentBuilder:
    while (true)
    {
      st.nextToken();

      switch ( st.ttype )
      {
        case StreamTokenizer.TT_EOF :
          // If we reach the end of the file and have no data to return,
          // set the EOF flag.
          if(element.length() == 0) EOF = true;
          break ContentBuilder;
        case StreamTokenizer.TT_EOL : break;
        case StreamTokenizer.TT_NUMBER : escapeLastChar = false; break;
        case StreamTokenizer.TT_WORD   : escapeLastChar = false; element.append(st.sval); break;
        default :
          if ( st.ttype == esc && escapeLastChar == false )
          {
            escapeLastChar = true;
          }
          else if ( st.ttype == esc && escapeLastChar == true )
          {
            element.append((char)st.ttype);
            escapeLastChar = false;
          }
          else if ( st.ttype == sep && escapeLastChar == true )
          {
            element.append((char)st.ttype);
            escapeLastChar = false;
          }
          else if ( st.ttype == sep && escapeLastChar == false )
          {
            break ContentBuilder;
          }
        ;
      }
    }

    content = element.toString();
  }

  private String commentString(String string)
  {
    switch (type)
    {
      case StreamParser.FIXED_LENGTH     : return string;
      case StreamParser.ESCAPED_STRING   : return commentEscapedString(string);
      case StreamParser.QUOTED_STRING    : return commentQuotedString(string);
      case StreamParser.SEPARATED_STRING : return string + sep;
      default : return string;
    }
  }

  private String commentEscapedString(String string)
  {
    StringBuffer sb = new StringBuffer();

    int pos = 0;
    int ix  = string.indexOf(esc);

    if (ix > -1)
    {
      while (ix != -1)
      {
        sb.append(string.substring(pos, ix) + esc + esc);
        pos = ix+1;
        ix = string.indexOf(esc, pos);
      }
      if (pos < string.length()) sb.append(string.substring(pos));
    }

    string = sb.toString();
    sb = new StringBuffer();

    pos = 0;
    ix  = string.indexOf(sep);

    if (ix > -1)
    {
      while (ix != -1)
      {
        sb.append(string.substring(pos, ix) + esc + sep);
        pos = ix+1;
        ix = string.indexOf(sep, pos);
      }
      if (pos < string.length()) sb.append(string.substring(pos));

      sb.append(sep);
    }

    return sb.toString() + sep;
  }

  private String commentQuotedString(String string)
  {
    if ( string.indexOf(sep) == -1 && string.indexOf(esc) == -1)
    {
      return string + sep;
    }
    else
    {
      StringBuffer sb = new StringBuffer();

      int pos = 0;
      int ix  = string.indexOf(esc);

      if (ix > -1)
      {
        while (ix != -1)
        {
          sb.append(string.substring(pos, ix) + esc + esc);
          pos = ix+1;
          ix = string.indexOf(esc, pos);
        }
        if (pos < string.length()) sb.append(string.substring(pos));
      }

      return (esc + sb.toString() + esc + sep);
    }
  }
}
