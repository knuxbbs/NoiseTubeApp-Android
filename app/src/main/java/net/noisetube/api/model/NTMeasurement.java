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

package net.noisetube.api.model;

import net.noisetube.api.tagging.TagsHelper;
import net.noisetube.api.util.URLUTF8Encoder;
import net.noisetube.api.util.XMLUtils;

import org.xml.sax.SAXException;

import java.io.Serializable;
import java.util.ArrayList;

import jlibs.xml.sax.XMLDocument;

/**
 * NoiseTube Measurement Model
 *
 * @author maisonneuve, mstevens, sbarthol, humberto
 */
public class NTMeasurement extends SLMMeasurement implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3399721560283243618L;
    NTLocation location = null;
    private ArrayList<String> userTags;


    public NTMeasurement() {
        super();
    }

    public NTMeasurement(long timeStampMS) {
        super(timeStampMS);
    }

    public boolean hasTags() {
        return (userTags != null);
    }

    public boolean hasUserTags() {
        return (userTags != null);
    }


    public void addUserTags(String userTypedTags) {
        String[] tags = TagsHelper.splitTypedTags(userTypedTags);
        if (tags.length > 0) {
            if (userTags == null) {
                userTags = new ArrayList<String>();
            }

            for (String tag : tags) {
                userTags.add(tag);
            }
        }
    }

    /**
     * @return the location
     */
    public NTLocation getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(NTLocation location) {
        this.location = location;
    }

    /**
     * String representation
     */
    public String toString() {
        return "date: "
                + timeStamp
                + (location != null ? "; location: " + location.toString() : "")
                + "; Leq: " + LeqDBA + " dB(A)";
    }

    /**
     * URL representation
     */
    @Override
    public String toUrl() {
        StringBuilder bff = new StringBuilder();
        bff.append("db=").append((int) LeqDBA.doubleValue());
        bff.append("&time=").append(
                URLUTF8Encoder.encode(XMLUtils.timeDateValue(timeStamp.getTime())));
        if (location != null) {
            bff.append("&l=").append(location.toString());
        }
        if (userTags != null) {
            bff.append("&tag=").append(
                    URLUTF8Encoder.encode(TagsHelper
                            .getSavableTagsString(userTags)));
        }

        return bff.toString();
    }

    /**
     * JSON representation
     * ["localtime(YYYY-MM-DDThh:mm:ss)",db,"location","tags space separated"
     * ,"autotags space separated"]
     */
    @Override
    public String toJSON() {

        final String bs = "\"", bse = "\",", empty = "", bb = "[", eb = "]";

        StringBuilder bff = new StringBuilder(bb);
        bff.append(bs).append(XMLUtils.timeDateValue(timeStamp.getTime()))
                .append(bse);
        bff.append(LeqDBA.doubleValue() + ",");
        bff.append(bs).append((location != null ? location.toString() : empty))
                .append(bse);
        bff.append(bs)
                .append((userTags != null ? TagsHelper
                        .getSavableTagsString(userTags) : empty)).append(bse);
        bff.append(bs)
                .append(empty).append(bs);

        bff.append(eb);
        return bff.toString();
    }

    /**
     * XML representation
     */
    @Override
    public void parseToXML(XMLDocument xml) throws SAXException {
        xml.startElement("measurement");
        xml.addAttribute("timeStamp", XMLUtils.timeDateValue(timeStamp.getTime()));
        xml.addAttribute("loudness", String.valueOf(LeqDBA));

        if (location != null && location.hasCoordinates())
            xml.addAttribute("location", location.toString());
        if (userTags != null)
            xml.addAttribute("tags", TagsHelper.getSavableTagsString(userTags));
        xml.endElement("measurement");
    }

    public ArrayList<String> getUserTags() {
        return userTags;
    }
}
