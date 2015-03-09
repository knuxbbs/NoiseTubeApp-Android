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

import org.xml.sax.SAXException;

import java.util.ArrayList;

import jlibs.xml.sax.XMLDocument;

/**
 * TaggedInterval class
 * <p/>
 * Indexes are 0-based and the interval _includes_ the begin and end index
 *
 * @author mstevens, sbarthol, humberto
 */
public class TaggedInterval implements Saveable {

    private int beginM;
    private int endM;
    private boolean automatic;
    private ArrayList<String> tags;

    public TaggedInterval() {

    }

    public TaggedInterval(int beginM, int endM, ArrayList<String> tags) {
        this(beginM, endM, tags, false);
    }

    public TaggedInterval(int beginM, int endM, ArrayList<String> tags, boolean automatic) {
        if (beginM > endM)
            throw new IllegalArgumentException("endMeasurement cannot come before beginMeasurement");
        this.beginM = beginM;
        this.endM = endM;
        this.automatic = automatic;
        this.tags = tags;
    }

    /**
     * @return the beginM
     */
    public int getBeginMeasurement() {
        return beginM;
    }

    public void setBeginMeasurement(int beginM) {
        this.beginM = beginM;
    }

    /**
     * @return the endM
     */
    public int getEndMeasurement() {
        return endM;
    }

    public void setEndMeasurement(int endM) {
        this.endM = endM;
    }

    /**
     * @return the automatic
     */
    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    /**
     * @return the tags
     */
    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    /* (non-Javadoc)
         * @see net.noisetube.model.SavableModel#toXML()
         */
    public void parseToXML(XMLDocument xml) throws SAXException {
        xml.startElement("taggedInterval");
        xml.addAttribute("beginIndex", String.valueOf(beginM));
        xml.addAttribute("endIndex", String.valueOf(endM));
        xml.addAttribute("tags", TagsHelper.getSavableTagsString(tags));
        xml.endElement("taggedInterval");
    }

    /*
     * Format: [beginM,endM,"tags space separated",automatic]
     *
     * (non-Javadoc)
     * @see net.noisetube.model.SavableModel#toJSON()
     */
    public String toJSON() {
        StringBuffer bff = new StringBuffer("[");
        bff.append(beginM + ",");
        bff.append(endM + ",");
        bff.append("\"" + TagsHelper.getSavableTagsString(tags) + "\",");
        bff.append(Boolean.toString(automatic));
        bff.append("]");
        return bff.toString();
    }

    /* (non-Javadoc)
     * @see net.noisetube.model.SavableModel#toUrl()
     */
    public String toUrl() {
        StringBuilder bff = new StringBuilder();
        bff.append("&beginIdx=").append(beginM);
        bff.append("&endIdx=").append(endM);
        if (automatic)
            bff.append("&autotag=").append(URLUTF8Encoder.encode(TagsHelper.getSavableTagsString(tags)));
        else
            bff.append("&tag=").append(URLUTF8Encoder.encode(TagsHelper.getSavableTagsString(tags)));
        return bff.toString();
    }

}