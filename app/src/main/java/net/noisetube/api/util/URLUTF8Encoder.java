/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.api.util;

/**
 * ------------------------------------------------------------------------------------------------
 * The source code below this notice, with the exception of the URLStringEncoder inner class, is
 * taken from the URLUTF8Encoder Java class distributed by the World Wide Web Consortium under
 * the terms of the W3C Software Notice and License.
 * The Java class source code as distributed: http://www.w3.org/International/URLUTF8Encoder.java
 * More information: http://www.w3.org/International/O-URL-code.html
 * <p/>
 * The W3C Software Notice and License is (L)GPL-compatible and the reuse of this code within the
 * LGPL v2.1-licensed NoiseTube Mobile Client is thereby permitted.
 * (http://www.gnu.org/licenses/license-list.html#GPLCompatibleLicenses)
 * <p/>
 * Below follows the original documentation/disclaimer comment of the URLUTF8Encoder Java class
 * as distributed by the W3C:
 * ------------------------------------------------------------------------------------------------
 * Provides a method to encode any string into a URL-safe form.
 * Non-ASCII characters are first encoded as sequences of two or three bytes, using the UTF-8
 * algorithm, before being encoded as %HH escapes.
 * <p/>
 * Created: 17 April 1997
 * Author: Bert Bos <bert@w3.org>
 * <p/>
 * URLUTF8Encoder: http://www.w3.org/International/URLUTF8Encoder.java
 * <p/>
 * Copyright � 1997 World Wide Web Consortium, (Massachusetts Institute of Technology, European
 * Research Consortium for Informatics and Mathematics, Keio University). All Rights Reserved.
 * This work is distributed under the W3C � Software License [1] in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p/>
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 * ------------------------------------------------------------------------------------------------
 */
public class URLUTF8Encoder {

    final static String[] hex = {"%00", "%01", "%02", "%03", "%04", "%05",
            "%06", "%07", "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e",
            "%0f", "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
            "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f", "%20",
            "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29",
            "%2a", "%2b", "%2c", "%2d", "%2e", "%2f", "%30", "%31", "%32",
            "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3a", "%3b",
            "%3c", "%3d", "%3e", "%3f", "%40", "%41", "%42", "%43", "%44",
            "%45", "%46", "%47", "%48", "%49", "%4a", "%4b", "%4c", "%4d",
            "%4e", "%4f", "%50", "%51", "%52", "%53", "%54", "%55", "%56",
            "%57", "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
            "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68",
            "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f", "%70", "%71",
            "%72", "%73", "%74", "%75", "%76", "%77", "%78", "%79", "%7a",
            "%7b", "%7c", "%7d", "%7e", "%7f", "%80", "%81", "%82", "%83",
            "%84", "%85", "%86", "%87", "%88", "%89", "%8a", "%8b", "%8c",
            "%8d", "%8e", "%8f", "%90", "%91", "%92", "%93", "%94", "%95",
            "%96", "%97", "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e",
            "%9f", "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
            "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af", "%b0",
            "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7", "%b8", "%b9",
            "%ba", "%bb", "%bc", "%bd", "%be", "%bf", "%c0", "%c1", "%c2",
            "%c3", "%c4", "%c5", "%c6", "%c7", "%c8", "%c9", "%ca", "%cb",
            "%cc", "%cd", "%ce", "%cf", "%d0", "%d1", "%d2", "%d3", "%d4",
            "%d5", "%d6", "%d7", "%d8", "%d9", "%da", "%db", "%dc", "%dd",
            "%de", "%df", "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6",
            "%e7", "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
            "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7", "%f8",
            "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"};

    /**
     * Encode a string to the "x-www-form-urlencoded" form, enhanced
     * with the UTF-8-in-URL proposal. This is what happens:
     * <p/>
     * <ul>
     * <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z',
     * and '0' through '9' remain the same.
     * <p/>
     * <li><p>The unreserved characters - _ . ! ~ * ' ( ) remain the same.
     * <p/>
     * <li><p>The space character ' ' is converted into a plus sign '+'.
     * <p/>
     * <li><p>All other ASCII characters are converted into the
     * 3-character string "%xy", where xy is
     * the two-digit hexadecimal representation of the character
     * code
     * <p/>
     * <li><p>All non-ASCII characters are encoded in two steps: first
     * to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
     * secondly each of these bytes is encoded as "%xx".
     * </ul>
     *
     * @param s The string to be encoded
     * @return The encoded string
     */
    public static String encode(String s) {
        StringBuffer sbuf = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            int ch = s.charAt(i);
            if ('A' <= ch && ch <= 'Z') {    //'A'..'Z'
                sbuf.append((char) ch);
            } else if ('a' <= ch && ch <= 'z') {    //'a'..'z'
                sbuf.append((char) ch);
            } else if ('0' <= ch && ch <= '9') {    //'0'..'9'
                sbuf.append((char) ch);
            } else if (ch == ' ') {    //space
                sbuf.append('+');
            } else if (ch == '-'
                    || ch == '_' //unreserved
                    || ch == '.' || ch == '!' || ch == '~' || ch == '*'
                    || ch == '\'' || ch == '(' || ch == ')') {
                sbuf.append((char) ch);
            } else if (ch <= 0x007f) {    //other ASCII
                sbuf.append(hex[ch]);
            } else if (ch <= 0x07FF) {    //non-ASCII <= 0x7FF
                sbuf.append(hex[0xc0 | (ch >> 6)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            } else {    //0x7FF < ch <= 0xFFFF
                sbuf.append(hex[0xe0 | (ch >> 12)]);
                sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
                sbuf.append(hex[0x80 | (ch & 0x3F)]);
            }
        }
        return sbuf.toString();
    }

    /**
     * @author mstevens
     */
    public static class URLStringEncoder implements IStringEncoder {

        public String encode(String plainText) {
            return URLUTF8Encoder.encode(plainText);
        }

    }

}
