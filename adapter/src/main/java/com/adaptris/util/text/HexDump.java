/*
 * $Id: HexDump.java,v 1.11 2004/07/19 13:59:39 lchan Exp $
 */
package com.adaptris.util.text;


/** A utility class to perform a hexdump of a given number of bytes.
 * @author $Author: lchan $
 */
public final class HexDump {
  private HexDump() {
  }

  /** Get a string suitable for printing out.
   *  <p>Equivalent to calling parse(b, 0, b.length);
   *  @see #parse(byte[], int, int)
   */
  public static String parse(byte[] bytes) {

    return parse(bytes, 0, bytes.length);
  }

  /**
   * Get a string suitable for printing out.
   * <p>
   * What you get is.
   * <p>
   * 
   * <pre>
   * {@code 
   * [54] [68] [65] [20] [71] [75] [69] [63] [6b] [20] [62] [72]    The quick br
   * [6f] [77] [6e] [20] [66] [6f] [78] [20] [6a] [75] [6d] [70]    own fox jump
   * [73] [20] [6f] [76] [65] [72] [20] [74] [68] [65] [20] [6c]    s over the l
   * [61] [7a] [79] [20] [64] [6f] [67]                             azy dog
   * Total Bytes 43
   * }
   * </pre>
   * </p>
   * <p>
   * Unprintable "ASCII" characters (&lt; 32, > 255) are represented by a "."
   * 
   * @return a string in the format described
   * @param bytes the bytes to create hexdump of
   * @param offset the offset to start from.
   * @param length the number of bytes to print out
   */
  public static String parse(byte[] bytes, int offset, int length) {
    StringBuffer sb = new StringBuffer();
    StringBuffer sb2 = new StringBuffer();
    StringBuffer data = new StringBuffer();
    int count = 0;

    if (offset > bytes.length - 1) {
      return "Request offset > byte array length";
    }

    if (offset + length > bytes.length) {
      return "Requested Length > byte array length";
    }

    for (int i = offset; i < offset + length; i++) {
      count++;

      int unsigned = unsignedByteToInt(bytes[i]);
      sb2.append(" [");
      sb2.append(Justify.leading(Integer.toHexString(unsigned), 2, '0'));
      sb2.append("]");
      data.append(
        unsigned < 32 || unsigned > 128 ? "." : new String(bytes, i, 1));

      if (count > 11) {

        String s = Justify.trailing(sb2.toString(), 64, ' ');
        data.append(System.getProperty("line.separator"));
        sb.append(s);
        sb.append(data.toString());
        sb2.setLength(0);
        data.setLength(0);
        count = 0;
      }
    }

    String s = Justify.trailing(sb2.toString(), 64, ' ');
    data.append(System.getProperty("line.separator"));
    sb.append(s);
    sb.append(data.toString());
    sb.append("Total bytes " + bytes.length);

    return sb.toString();
  }

  private static int unsignedByteToInt(byte b) {
    return b & 0xFF;
  }

}
