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

package net.noisetube.app.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.api.TrackStatistics;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.NTColor;
import net.noisetube.api.util.Logger;
import net.noisetube.api.util.MathNT;
import net.noisetube.api.util.StringUtils;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.core.AndroidNTService;

;

/**
 * @author mstevens, humberto
 */
public class TrackSummaryDialog extends Dialog {

    private static TrackSummaryDialog instance;
    private Track track;
    private boolean saving;
    private TextView txtWaiting;
    private Button btnOK;
    private Button btnCancel;
    private ProgressBar progressBar;
    private LinearLayout actionContainer;
    private TableRow container;
    private AndroidNTService service;

    public TrackSummaryDialog(Context context, Track track, boolean saving) {
        super(context);

        this.track = track;
        this.saving = saving;
        service = AndroidNTService.getInstance();

    }

    public static TrackSummaryDialog getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setCancelable(!saving); //will be set to true in stopWaiting()
        setContentView(R.layout.dialog_track_summary);
        setTitle(R.string.title_dialog_summary);
        progressBar = (ProgressBar) findViewById(R.id.uploading_progressBar);
        //Button & events:
        btnOK = (Button) findViewById(R.id.btnSummaryOK);

        try {

            btnOK.setOnClickListener(new View.OnClickListener() {
                public void onClick(View btn) {
                    service.resetTrack();
                    cancel();
                }
            });

            if (AndroidPreferences.getInstance().getAccount() == null) {
                btnOK.setEnabled(true);
            }

            container = (TableRow) findViewById(R.id.progress_container);
            if (!saving) {

                container.setVisibility(View.GONE);
//                btnCancel.setVisibility(View.GONE);
                btnOK.setVisibility(View.VISIBLE);
            }

            txtWaiting = (TextView) findViewById(R.id.txtSummaryWaiting);


            if (saving)
                txtWaiting.setText("Waiting (max. " + Math.round(Track.WAIT_FOR_SAVING_TO_COMPLETE_MS / 1000f) + "s) for last measurements to be saved...");

            String id = (track.getTrackID() == -1) ? "PENDING" : Integer.toString(track.getTrackID());
            ((TextView) findViewById(R.id.txtTrackID)).setText(id);

            ((TextView) findViewById(R.id.txtSummaryElapsedTime)).setText(StringUtils.formatTimeSpanColons(track.getTotalElapsedTime()));

            //Statistics:
            TrackStatistics stats = track.getStatistics();
            ((TextView) findViewById(R.id.txtSummaryNrOfMeasurements)).setText(Integer.toString(stats.getNumMeasurements()));
            ((TextView) findViewById(R.id.lblSummaryMinLeq)).setText(Html.fromHtml("Minimum <i>L</i><sub>Aeq,1s</sub>:"));
            ((TextView) findViewById(R.id.txtSummaryMinLeq)).setText(MathNT.roundTo(stats.getMindBA(), 2) + " dB(A)");
            ((TextView) findViewById(R.id.lblSummaryMaxLeq)).setText(Html.fromHtml("Maximum <i>L</i><sub>Aeq,1s:"));
            ((TextView) findViewById(R.id.txtSummaryMaxLeq)).setText(MathNT.roundTo(stats.getMaxdBA(), 2) + " dB(A)");
            ((TextView) findViewById(R.id.lblSummaryAvgLeq)).setText(Html.fromHtml("Average <i>L</i><sub>Aeq,1s:"));
            ((TextView) findViewById(R.id.txtSummaryAvgLeq)).setText(MathNT.roundTo(stats.getLogAvrdBA(), 2) + " dB(A)");
            ((TextView) findViewById(R.id.txtSummaryDistance)).setText(StringUtils.formatDistance(stats.getDistanceCovered(), -2));
        } catch (Exception e) {
            (Logger.getInstance()).error(e, "Error on showing track summary");
            cancel();
        }


    }

    public void stopWaiting(boolean savingCompleted) {
        setCancelable(true); //!!!

        NTColor color;
        int msg;

        if (savingCompleted) {
            color = new NTColor(200, 230, 201);
            msg = R.string.summary_waiting_done;
        } else {
            color = new NTColor(255, 205, 210);
            msg = R.string.summary_waiting_failed;
        }

        container.setBackgroundColor(color.getRGBValue());
        progressBar.setVisibility(View.GONE);
        txtWaiting.setText(msg);
        btnOK.setEnabled(true);
        service.updateTrackID(track.hashCode(), track.getTrackID());


    }

}
