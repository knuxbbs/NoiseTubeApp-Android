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

package net.noisetube.api.ui;

import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.SLMMeasurement;
import net.noisetube.api.model.SoundLevelScale;
import net.noisetube.api.model.Track;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Sound Pressure Level (SPL) graph
 *
 * @author mstevens, humberto
 */
public class SPLGraph {

    public static final int ALIGN_RIGHT = 0;
    private int labelAlignment = ALIGN_RIGHT;
    //public static final int ALIGN_CENTER = 2;
    public static final int ALIGN_LEFT = 1;
    public static final boolean DEFAULT_HAIRLINES = true;
    private boolean hairLines = DEFAULT_HAIRLINES;
    protected int yLabelsMargin; //in pixels
    ArrayList<Float> selectionXs = new ArrayList<Float>();
    ArrayList<Float> selectionYs = new ArrayList<Float>();
    private int width = 0;
    private int height = 0;
    private GUI gui;
    private double minimumDB = 25D;
    private double maximumDB = 105D;
    private Track track = null;
    private boolean selecting;
    private float minX;
    private float maxX;
    private float scaleXpx, scaleYpx; //pixels per value unit

    public SPLGraph(GUI gui, double minimumDB, double maximumDB) {
        this.gui = gui;
        this.minimumDB = minimumDB;
        this.maximumDB = maximumDB;
        this.selecting = false;
    }

    /**
     * @return the hairLines
     */
    public boolean isHairLines() {
        return hairLines;
    }

    /**
     * @param hairLines the hairLines to set
     */
    public void setHairLines(boolean hairLines) {
        this.hairLines = hairLines;
    }

    /**
     * @return the minimumDB
     */
    public double getMinimumDB() {
        return minimumDB;
    }

    /**
     * @param minimumDB the minimumDB to set
     */
    public void setMinimumDB(double minimumDB) {
        this.minimumDB = minimumDB;
    }

    /**
     * @return the maximumDB
     */
    public double getMaximumDB() {
        return maximumDB;
    }

    /**
     * @param maximumDB the maximumDB to set
     */
    public void setMaximumDB(double maximumDB) {
        this.maximumDB = maximumDB;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    /**
     * This calculates the actual amount of pixels that should be used to represent one decibel.
     * It also calculates the width between the displayed measurements.
     */
    private void updateDimensions() {
        width = gui.getWidth();
        height = gui.getHeight();
        yLabelsMargin = (int) gui.getLabelWidth(Integer.toString(((int) maximumDB / 10) * 10), true) + 2;
        if (track != null)
            scaleXpx = ((float) width - yLabelsMargin) / track.getBufferCapacity();
        else
            scaleXpx = 1;
        scaleYpx = (float) ((height - 1) / (maximumDB - minimumDB));
        if (scaleYpx == 0)
            scaleYpx = 1;
    }

    /**
     * @return the yLabelsMargin
     */
    public int getYLabelsMargin() {
        return yLabelsMargin;
    }

    public void initGraph() {
        updateDimensions(); //!!!


        NTColor color, labelColor = new NTColor(102, 102, 102);
        //Colored line each 10dB, starting from the first multiple of 10 above the minimumDB till and including the biggest multiple of 10  below maximumDB
        for (int l = (((int) minimumDB / 10) + 1); l <= ((int) maximumDB / 10); l++) {
            int db = l * 10;
            color = SoundLevelScale.getColor(db); //used to be gray (0x555555)
            //Line:
            float y = (float) (height - 1 - ((db - minimumDB) * scaleYpx));
            gui.drawLine(color, yLabelsMargin, y, width - 1, y, false, hairLines);
            //Label:
            String lbl = Integer.toString((int) db);
            gui.drawLabel(lbl,
                    labelColor,
                    (labelAlignment == ALIGN_LEFT ? 0 : yLabelsMargin - 2 - gui.getLabelWidth(lbl, true)),
                    (float) (height - ((db - minimumDB) * scaleYpx) + (gui.getLabelHeight(lbl, true) / 2)),
                    true);
        }
    }

    public void draw() {

        initGraph();
        //Measurements:
        if (track == null || track.getBufferSize() < 2)
            return;
        Enumeration<SLMMeasurement> e = track.getMeasurements();
        int count = 0;
        NTMeasurement next = (NTMeasurement) e.nextElement();
        NTMeasurement current;


        //selection surface coordinates:
        // removing older selection
        selectionXs.clear();
        selectionYs.clear();
        selectionXs.add((float) 0);
        selectionYs.add((float) 0);

        //tagged interval surface coordinates:
        //TODO draw intervals as surfaces
        int i = 0;
        while (e.hasMoreElements()) {

            i++;
            current = next;
            next = (NTMeasurement) e.nextElement();
            double currentOffset = (current.isLeqDBASet() ? current.getLeqDBA() : current.getLeqDB()) - minimumDB;
            double nextOffset = (next.isLeqDBASet() ? next.getLeqDBA() : next.getLeqDB()) - minimumDB;

            float x1 = yLabelsMargin + ((i - 1) * scaleXpx);
            float y1 = (float) (height - 1 - (currentOffset * scaleYpx));
            float x2 = yLabelsMargin + (i * scaleXpx);
            float y2 = (float) (height - 1 - (nextOffset * scaleYpx));

            gui.drawLine(new NTColor(66, 66, 6), //grey
                    x1,
                    y1,
                    x2,
                    y2,
                    true,
                    hairLines);
            //Indicate tags with horizontal lines:
            if (current.hasUserTags() || track.isIntervalTaggedUser(current.getNumber())) {
                gui.drawLine(new NTColor(25, 118, 210) /*blue*/, x1, 0, x1, height - 1, false, hairLines);
            }


            //Needed to store the points of the graph that are selected,
            //so the surface underneath can later on be marked using drawSurface(...)
            if (selecting) {
                if (minX <= x2 && maxX >= x1) {
                    selectionXs.add(x1);
                    selectionYs.add(y1);
                    if (!e.hasMoreElements() || !(maxX >= x2)) {
                        //To make sure that also the last point is added
                        selectionXs.add(x2);
                        selectionYs.add(y2);
                    }
                }
            }
        }

        if (selecting && (selectionXs.size() > 1)) {
            //Adding the bottom left and bottom right points of the surface.
            //Up until now you only have the points of the graph line stored in xs and ys
            selectionXs.set(0, selectionXs.get(1));
            selectionYs.set(0, (float) height);
            selectionXs.add(selectionXs.get(selectionXs.size() - 1));
            selectionYs.add((float) height);

            //draw selection surface:
            gui.drawSurface(new NTColor(187, 222, 251), //182,182,182
                    selectionXs,
                    selectionYs,
                    true);
        }
    }

    //Code to be used in combination with a touch-screen: when selecting a segment of the graph
    public void beginSelecting() {
        selecting = true;
        minX = 0;
        maxX = 0;
    }

    public void endSelecting() {
        selecting = false;
        minX = 0;
        maxX = 0;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }


    /**
     * Interface to be implemented by GUI class which displays the SPLGraph
     *
     * @author mstevens, sbarthol
     */
    public interface GUI {

        public SPLGraph getSoundLevelGraph();

        public int getHeight();

        public int getWidth();

        public float getLabelWidth(String label, boolean antiAlias);

        public float getLabelHeight(String label, boolean antiAlias);

        public void drawLine(NTColor color, float x1, float y1, float x2, float y2, boolean antiAlias, boolean hairline);

        public void drawLabel(String label, NTColor labelColor, float x, float y, boolean antiAlias);

        public void drawSurface(NTColor color, ArrayList<Float> xs, ArrayList<Float> ys, boolean antiAlias);

    }

}
