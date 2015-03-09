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

import org.xml.sax.SAXException;

import java.io.Serializable;
import java.util.Date;

import jlibs.xml.sax.XMLDocument;

/**
 * AudioMeasurement
 * <p/>
 * Superclass for NTMeasurement, with only time and Leq/LeqA
 * Only directly used in NT SLM Library
 *
 * @author mstevens
 */
public class SLMMeasurement implements Saveable, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6842930778668459646L;
    protected Date timeStamp;
    protected Double LeqDBA;
    private Double LeqDB;
    private int number = 0; // number of measurement in the current track

    public SLMMeasurement() {
        timeStamp = new Date(System.currentTimeMillis());
    }

    public SLMMeasurement(long timeStampMS) {
        this.timeStamp = new Date(timeStampMS);
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the LeqDB
     */
    public double getLeqDB() {
        return LeqDB.doubleValue();
    }

    /**
     * @param LeqDB the LeqDB to set
     */
    public void setLeqDB(double LeqDB) {
        this.LeqDB = new Double(LeqDB);
    }

    public boolean isLeqDBSet() {
        return LeqDB != null;
    }

    /**
     * @return the LeqDBA
     */
    public double getLeqDBA() {
        return LeqDBA.doubleValue();
    }

    /**
     * @param LeqDBA the LeqDBA to set
     */
    public void setLeqDBA(double LeqDBA) {
        this.LeqDBA = new Double(LeqDBA);
    }

    public boolean isLeqDBASet() {
        return LeqDBA != null;
    }

    /**
     * String representation
     */
    public String toString() {
        return "date: " + timeStamp + "; Leq: " + LeqDBA + " dB(A)";
    }


    @Override
    public void parseToXML(XMLDocument xml) throws SAXException {

    }

    @Override
    public String toJSON() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}