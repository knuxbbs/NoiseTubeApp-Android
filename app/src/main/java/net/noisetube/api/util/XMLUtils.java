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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A class with helpful methods for dealing with XML
 *
 * @author mstevens
 */
public class XMLUtils {

    /**
     * Returns an XML comment string with the given text and the given number of
     * tabs in front
     *
     * @param text
     * @param tabs
     * @return xml comment String
     */
    static public String comment(String text, int tabs) {
        return StringUtils.addTabsFront("<!-- " + escapeCharacters(text) + " -->", tabs);
    }

    /**
     * Replaces reserved XML characters with escapes
     *
     * @param input a String to process
     * @return the same String but with reserved XML characters escaped
     */
    static public String escapeCharacters(String input) {
        input = StringUtils.replace(input, '&', "&amp;");
        input = StringUtils.replace(input, '<', "&lt;");
        input = StringUtils.replace(input, '>', "&gt;");
        input = StringUtils.replace(input, '"', "&quot;");
        input = StringUtils.replace(input, '\'', "&apos;");
        return input;
    }

    /**
     * Converts a long timestamp to a string in XML dateTime format:</br>
     * - dd.MM.yyyy HH:mm:ss</br>
     *
     * @param timeStamp
     * @return A String containing the formatted timestamp
     */
    public static String timeStampToString(long timeStamp) {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
        return formatter.format(new Date(timeStamp));


    }

    public static String dateToString(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
        return formatter.format(date);


    }

    public static Date stringToDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
        return formatter.parse(date);


    }

    static public String timeDateValue(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault()); //use system time zone
        cal.setTime(new Date(timeStamp)); //set the time
        //Format the string:
        StringBuffer bff = new StringBuffer();
        //Date (YYYY-MM-DD) << maxedit: this should be (yyyy-MM-dd)
        bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal.get(Calendar.YEAR)), 4) + "-");
        bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal.get(Calendar.MONTH) + 1), 2) + "-");
        bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 2));
        //Time (Thh:mm:ss) << maxedit: this is actually Time(THH:mm:ss), because HOUR_OF_DAY returns 24 hours not am/pm and HH = 24 whereas hh = am/pm
        bff.append("T" + StringUtils.padWithLeadingZeros(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2) + ":");
        bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal.get(Calendar.MINUTE)), 2) + ":");
        bff.append(StringUtils.padWithLeadingZeros(String.valueOf(cal.get(Calendar.SECOND)), 2));
        //UTC/GTM offset if needed:
        int gmtOffset = cal.getTimeZone().getRawOffset();
        if (gmtOffset == 0)
            //time is already in UTC/GMT (add "Z")
            bff.append("Z");
        else {    //time is not in UTC/GMT (add zzzzzz, which represents hh:mm in relation to UTC)
            bff.append((gmtOffset < 0) ? "-" : "+"); // + or -
            bff.append(StringUtils.padWithLeadingZeros(String.valueOf(Math.abs(gmtOffset) / 3600000), 2) + ":"); // offset in whole absolute hours
            bff.append(StringUtils.padWithLeadingZeros(String.valueOf((Math.abs(gmtOffset) % 3600000) / 60000), 2)); //modulo of whole hour offset in absolute minutes
        }
        return bff.toString(); //return the result
    }

    public static class XMLStringEncoder implements IStringEncoder {

        public String encode(String plainText) {
            return XMLUtils.escapeCharacters(plainText);
        }

    }

}
