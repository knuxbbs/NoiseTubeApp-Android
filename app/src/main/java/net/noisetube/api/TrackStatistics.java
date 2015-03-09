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

package net.noisetube.api;

import net.noisetube.api.model.NTCoordinates;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Track;
import net.noisetube.api.util.MathNT;
import net.noisetube.api.util.StringUtils;

/**
 * @author mstevens
 */
public class TrackStatistics implements Processor {

    static private final char SEPARATOR = '$';
    //NumMeasurements
    private int numMeasurements = 0;
    //dB(A)
    private double avrdBA = 0; //arithmetic mean

    //dB
    //private double avrdB = 0; //arithmetic mean
    //private double logAvrdB = 0; //logaritmic average (= correct way)
    //private double maxdB = 0;
    //private double mindB = Double.MAX_VALUE;
    private double logAvrdBA = 0; //logaritmic average (= correct way)
    private double maxdBA = 0;
    private double mindBA = Double.MAX_VALUE;
    //Distance covered
    private NTCoordinates previousCoordinates = null;
    private float distanceMeters = 0;

    /**
     * For restarted Tracks
     */
    static public TrackStatistics parse(String serialisedMeasurementStatistics) {
        String[] parts = StringUtils.split(serialisedMeasurementStatistics, SEPARATOR);
        if (parts == null || parts.length < 5)
            throw new IllegalArgumentException("Invalid runstate string: " + serialisedMeasurementStatistics);
        TrackStatistics stats = new TrackStatistics();
        stats.numMeasurements = Integer.parseInt(parts[0]);
        stats.avrdBA = Double.parseDouble(parts[1]);
        stats.logAvrdBA = Double.parseDouble(parts[2]);
        stats.maxdBA = Double.parseDouble(parts[3]);
        stats.mindBA = Double.parseDouble(parts[4]);
        stats.distanceMeters = Float.parseFloat(parts[5]);
        return stats;
    }

    public void process(NTMeasurement measurement, Track track) {
        numMeasurements++;
        /*//dB
        if(measurement.isLoudnessLeqDBSet())
		{
			double db = measurement.getLoudnessLeqDB();
			//MAX
			if(db > maxdB)
				maxdB = db;
			//MIN
			if(db < mindB)
				mindB = db;
			//ordinary average (= arithmetic mean)
			avrdB = ((avrdB * (numMeasurements - 1)) + db) / numMeasurements;
			//log-average
			double prevInnerSum = (numMeasurements - 1) * MathNT.pow(10d, (logAvrdB / 10d));
			logAvrdB = 10d * MathNT.log10((prevInnerSum + MathNT.pow(10d, (db / 10d))) / numMeasurements);
		}*/
        //dB(A)
        if (measurement.isLeqDBASet()) {
            double dba = measurement.getLeqDBA();
            //MAX
            if (dba > maxdBA)
                maxdBA = dba;
            //MIN
            if (dba < mindBA)
                mindBA = dba;
            //ordinary average (= arithmetic mean)
            avrdBA = ((avrdBA * (numMeasurements - 1)) + dba) / numMeasurements;
            //log-average
            double prevInnerSum = (numMeasurements - 1) * MathNT.pow(10d, (logAvrdBA / 10d));
            logAvrdBA = 10d * MathNT.log10((prevInnerSum + MathNT.pow(10d, (dba / 10d))) / numMeasurements);
        }
        //Distance:
        if (measurement.getLocation() != null && measurement.getLocation().hasCoordinates()) {
            NTCoordinates coords = measurement.getLocation().getCoordinates();
            if (previousCoordinates != null)
                distanceMeters += previousCoordinates.distanceTo(coords);
            previousCoordinates = coords;
        }
    }

    /**
     * @return the numMeasurements
     */
    public int getNumMeasurements() {
        return numMeasurements;
    }

    public String getFormattedMinMAxAvgValue() {
        StringBuilder sb = new StringBuilder();
        sb.append((int) mindBA).append('/').append((int) maxdBA).append('/').append((int) logAvrdBA);
        return sb.toString();
    }

    public String getFormattedCoveredDistance() {
        return (distanceMeters == 0 ? "0 m" : StringUtils.formatDistance(distanceMeters, -2));
    }


    /**
     * @return the avrdBA
     */
    public double getAvrdBA() {
        return avrdBA;
    }

    /**
     * @return the logAvrdBA
     */
    public double getLogAvrdBA() {
        return logAvrdBA;
    }

    /**
     * @return the maxdBA
     */
    public double getMaxdBA() {
        return maxdBA;
    }

    /**
     * @return the mindBA
     */
    public double getMindBA() {
        return mindBA;
    }

    /**
     * @return the distanceMeters
     */
    public float getDistanceCovered() {
        return distanceMeters;
    }

    public String serialize() {
        return Integer.toString(numMeasurements) + SEPARATOR +
                Double.toString(avrdBA) + SEPARATOR +
                Double.toString(logAvrdBA) + SEPARATOR +
                Double.toString(maxdBA) + SEPARATOR +
                Double.toString(mindBA) + SEPARATOR +
                Float.toString(distanceMeters);
    }

    public String prettyPrint() {
        return " - Measurements: " + numMeasurements +
                "\n - Average Leq(1s): " + logAvrdBA + " dB(A)" +
                "\n - Arithmetic mean Leq(1s): " + avrdBA + " dB(A)" +
                "\n - Maximum Leq(1s): " + maxdBA + " dB(A)" +
                "\n - Minimum Leq(1s): " + mindBA + " dB(A)" +
                "\n - Distance covered: " + StringUtils.formatDistance(distanceMeters, -2);
    }

    public void reset() {
        numMeasurements = 0;

        //dB
        //avrdB = 0;
        //logAvrdB = 0;
        //maxdB = 0;
        //mindB = Double.MAX_VALUE;

        //dB(A)
        avrdBA = 0;
        logAvrdBA = 0;
        maxdBA = 0;
        mindBA = Double.MAX_VALUE;

        //Distance covered
        previousCoordinates = null;
        distanceMeters = 0;
    }

    public String getName() {
        return "Measurement statistics";
    }

}
