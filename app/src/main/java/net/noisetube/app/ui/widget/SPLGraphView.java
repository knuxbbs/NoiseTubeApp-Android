/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2015
 *  Portions contributed by University College London (ExCiteS group), 2012
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2015
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

package net.noisetube.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.noisetube.api.model.Track;
import net.noisetube.api.ui.NTColor;
import net.noisetube.api.ui.SPLGraph;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.TagMeasureDialog;
import net.noisetube.app.ui.delegate.MeasureViewModel;
import net.noisetube.app.ui.listener.Notification;
import net.noisetube.app.ui.listener.NotificationListener;

import java.util.ArrayList;

/**
 * Android Sound Level Pressure (SPL) graph
 * <p/>
 * Used to display a graph showing the recorded dbA output, an actual integer
 * of this dbA and the amount of measurements made.
 *
 * @author mstevens, sbarthol, humberto
 */
public class SPLGraphView extends View implements SPLGraph.GUI {

    private final static int SELECT_UPDATE_RATE = 100;
    private static int LABEL_TEXT_SIZE = 22;
    private static float LINE_STROKE_WIDTH_HAIRLINE = 0.0f;
    private static float LINE_STROKE_WIDTH_THICKER = 2.0f;
    private static int MIN_DB = 25;
    private static int MAX_DB = 105;
    private SPLGraph splG;
    private float startX;
    private float currentX;
    private long lastUpdate;


    private Paint linePaint;
    private Paint labelPaint;
    private Paint surfacePaint;
    private Canvas canvas;
    private boolean enableTouchEvent = false;
    private MeasureViewModel parentDelegate;
    private NotificationListener parentListener;


    public SPLGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.splG = new SPLGraph(this, MIN_DB, MAX_DB);
        splG.setHairLines(false); //use thicker lines on Android (TODO: make this dependent on screen density)
        this.linePaint = new Paint();
        this.labelPaint = new Paint();
        labelPaint.setFakeBoldText(true); //TODO make this dependent on screen density?
        this.surfacePaint = new Paint();
    }

    public void drawTrack(Track t) {
        splG.setTrack(t);
        invalidate();
        requestLayout();
    }


    /**
     * onDraw is called whenever this view is drawn.
     *
     * @param canvas The canvas bound to this view.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        splG.draw();
    }

    public SPLGraph getSoundLevelGraph() {
        return splG;
    }

    public float getLabelWidth(String label, boolean antiAlias) {
        labelPaint.setAntiAlias(antiAlias);
        labelPaint.setTextSize(LABEL_TEXT_SIZE);
        return labelPaint.measureText(label);
    }

    public float getLabelHeight(String label, boolean antiAlias) {
        labelPaint.setAntiAlias(antiAlias);
        labelPaint.setTextSize(LABEL_TEXT_SIZE);
        Rect labelBounds = new Rect();

        labelPaint.getTextBounds(label, 0, label.length(), labelBounds);
        return labelBounds.height() - (labelPaint.isFakeBoldText() ? 4 : 0); //HACK(ISH)
    }

    public void drawLine(NTColor color, float x1, float y1, float x2, float y2, boolean antiAlias, boolean hairline) {
        linePaint.setStrokeWidth(hairline ? LINE_STROKE_WIDTH_HAIRLINE : LINE_STROKE_WIDTH_THICKER);
        linePaint.setAntiAlias(antiAlias);
        linePaint.setColor(color.getARGBValue());
        canvas.drawLine(x1, y1, x2, y2, linePaint);
        invalidate();
        requestLayout();
    }

    public void drawSurface(NTColor color, ArrayList<Float> fXs, ArrayList<Float> fYs, boolean antiAlias) {

        Path path = new Path();
        path.moveTo(fXs.get(0), fYs.get(0));
        for (int i = 1; i < fXs.size(); i++) {
            path.lineTo(fXs.get(i), fYs.get(i));
        }
        path.lineTo(fXs.get(0), fYs.get(0));
        path.close();
        path.setFillType(Path.FillType.EVEN_ODD);

        surfacePaint.setStyle(Style.FILL_AND_STROKE);
        surfacePaint.setAntiAlias(antiAlias);
        surfacePaint.setColor(color.getARGBValue());
        canvas.drawPath(path, surfacePaint);
        invalidate();
        requestLayout();

    }

    public void drawLabel(String label, NTColor labelColor, float x, float y, boolean antiAlias) {
        labelPaint.setAntiAlias(antiAlias);
        labelPaint.setColor(labelColor.getARGBValue());
        labelPaint.setTextSize(LABEL_TEXT_SIZE);
        canvas.drawText(label, x, y, labelPaint);
        invalidate();
        requestLayout();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enableTouchEvent) {
            float X = event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    splG.beginSelecting();
                    startX = X;
                    currentX = X;
                    //pause for selection:
                    if (!parentDelegate.isPaused())
                        parentDelegate.invokePauseAction();
                    break;

                case MotionEvent.ACTION_MOVE:
//				//To not overload the splG class with updates every few milliseconds
                    final long currentTime = System.currentTimeMillis();
                    if ((currentTime - lastUpdate) < SELECT_UPDATE_RATE)
                        break;
                    lastUpdate = currentTime;
                    currentX = X;
                    if (currentX < startX) {
                        splG.setMinX(currentX);
                        splG.setMaxX(startX);
                    } else {
                        splG.setMinX(startX);
                        splG.setMaxX(currentX);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    splG.endSelecting();
                    //resume if allowed:
                    if (!AndroidPreferences.getInstance().isPauseDuringTagTyping() && parentDelegate.isPaused())
                        parentDelegate.invokeResumeAction();
                    if (currentX < 0) currentX = 0;
                    if (startX < 0) startX = 0;
                    if (currentX < startX)
                        tagSegment(currentX, startX);
                    else
                        tagSegment(startX, currentX);

                    parentListener.notify(new Notification());
                    startX = 0;
                    currentX = 0;
                    break;
            }

            invalidate();
            requestLayout();

        }

        return true;
    }

    public void setParentDelegate(MeasureViewModel parentDelegate) {
        this.parentDelegate = parentDelegate;
    }

    public void setEnableTouchEvent(boolean enableTouchEvent) {
        this.enableTouchEvent = enableTouchEvent;
    }

    public void setParentListener(NotificationListener parentListener) {
        this.parentListener = parentListener;
    }


    private void tagSegment(float minX, float maxX) {
        int firstMeasurementNbr = 0;
        int lastMeasurementNbr = 0;
        Track track = parentDelegate.getTrack();

        if (track != null) {
            int numberOfMeasurements = track.getBufferCapacity();
            int numberOfSegments = numberOfMeasurements - 1;

            //We must always take into account that the graph starts slightly to the right
            double segmentWidth = (double) ((getWidth() - splG.getYLabelsMargin()) / (double) numberOfSegments);
            double minXOnGraph = minX - splG.getYLabelsMargin();
            if (minXOnGraph < 0) minXOnGraph = 0.0;
            double maxXOnGraph = maxX - splG.getYLabelsMargin();
            if (maxXOnGraph < 0) maxXOnGraph = 0.0;

            //Round downwards
            //Location start at 0 for first measurement
            firstMeasurementNbr = (int) Math.floor(minXOnGraph / segmentWidth);
            lastMeasurementNbr = firstMeasurementNbr;
            //If the selection starts to far to the right (where there are no measurements yet), nothing should be tagged
            if (firstMeasurementNbr >= track.getBufferSize())
                return;
            for (int i = firstMeasurementNbr; i < numberOfMeasurements; i++) {
                lastMeasurementNbr = i + 1;
                //leftBarrier is the lowest x-value of the current segment
                //double leftBarrier = i*segmentWidth;
                //rightBarrier is the highest x-value of the current segment
                double rightBarrier = (i + 1) * segmentWidth;
                if (rightBarrier > maxXOnGraph)
                    break;
            }

            //If we ended selecting to far to the right (where there are no measurements yet), the selection should only go to the last measurement
            if (lastMeasurementNbr >= track.getBufferSize())
                lastMeasurementNbr = track.getBufferSize() - 1;
            if (firstMeasurementNbr <= lastMeasurementNbr) {
                new TagMeasureDialog(getContext(), track, firstMeasurementNbr, lastMeasurementNbr).show();
            }
            // redraw the canvas if necessary
            invalidate();
            requestLayout();
        }
    }


    public void reset() {
        splG.setTrack(null);
        invalidate();
        requestLayout();

    }
}
