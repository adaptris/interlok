package com.adaptris.transform.ff;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * 
 * @author sellidge
 */
public abstract class Handler {
  protected transient static final Logger logR = LoggerFactory.getLogger(Handler.class);
  protected transient static final Logger logP = LoggerFactory.getLogger(Handler.class);

  protected boolean DEBUG = false;
  // protected PrintStream LogOut = System.out;
  protected SimpleDateFormat formatter = new SimpleDateFormat(
      "':'yyMMdd':'hh.mm.ss':'");

  protected int COUNT = 0;

  /** Repetitions counter interface : set initial counter
   */
  abstract void setCount(int count);

  /** Repetitions counter interface
   *  @return number of repetitions
   */
  abstract int getCount();

  /** Determines whether this is current record or not
   * 
   * @param message the message.
   * @param rewind whether to rewind to the beginning of the stream or not
   * @return true or false
   */
  abstract boolean isThisHandler(StreamParser message, boolean rewind);

  /** Message parser
   * 
   * @param sp the input
   * @param output the output
   */
  abstract void process(StreamParser sp, PrintWriter output);

  /**
   * Wrapper for the log method. Only prints if DEBUG is set
   * 
   * @param message - the message to be logged
   */
  protected void debug(String message) {
    logR.debug(message);
  }

  /**
   * Method which prints out a message to OutputStream LogOut. Message is
   * formatted as: <type>:yymmdd:hh.mm.ss:<message>
   * 
   * @param type - descriptive string, up to 7 chars
   * @param message - string to be logged
   */

  protected void log(String type, String message) {
    if (message == null) {
      logR.info(message);
    }
    else if (message.equals("INFO")) {
      logR.info(message);
    }
    else if (message.equals("WARNING")) {
      logR.warn(message);
    }
    else if (message.equals("ERROR")) {
      logR.error(message);
    }
    else if (message.equals("DEBUG")) {
      logR.debug(message);
    }
    else {
      logR.info(message);
    }
  }

  /**
   * Method which prints out a message to OutputStream LogOut. Message is
   * formatted as: INFO :yymmdd:hh.mm.ss:<message>
   * 
   * @param message - string to be logged
   */

  protected void log(String message) {
    logR.info(message);
  }

  /**
   * Returns a string padded out to a specified length
   * 
   * @param input - string to be padded
   * @param len - length to pad string to
   * @param pad - character to pad string with
   * @return input string padded to correct length
   */
  protected static String padChar(String input, int len, String pad) {
    String output = new String();

    if (len > 0) {
      if (input.length() > len) {
        output = input.substring(0, len);
      }
      else {
        output = input;
        for (int i = input.length(); i < len; i++) {
          output += pad;
        }
      }
    }
    else {
      output = input;
    }

    return output;
  }

  /**
   * returns a specified attribute from an input DOM node or an empty string if
   * the parameter isn't in the file. Currently only capable of correctly
   * parsing \n as a separator
   * 
   * @param node - the node to retrieve the attribute from
   * @param name - the name of the attribute to return
   * @return String representation of attribute
   */
  protected static String getAttribute(Node node, String name) {
    String retVal = new String();
    try {
      retVal = node.getAttributes().getNamedItem(name).getNodeValue();

      if (retVal.equals("\\n")) {
        retVal = "\n";
      }
    }
    catch (Exception e) {
      retVal = "";
    }

    return retVal;
  }

  /**
   * Returns specified integer attribute from input node.
   * 
   * @param node - the node to retrieve the attribute from
   * @param name - the name of the attribute to return
   * @return int representation of attribute or 0 if not present
   */
  protected static int getIntAttribute(Node node, String name) {
    try {
      return Integer.parseInt(getAttribute(node, name));
    }
    catch (Exception e) {
      return 0;
    }
  }

  /**
   * Returns whether this handler is optional or not default is true. Classes
   * which extend this class can override to enable conditional processing
   */
  public boolean isOptional() {
    return true;
  }
}