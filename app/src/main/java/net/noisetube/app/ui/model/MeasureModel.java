package net.noisetube.app.ui.model;

import net.noisetube.api.model.Track;
import net.noisetube.api.ui.TrackUI;
import net.noisetube.app.core.AndroidNTService;

/**
 * @author Humberto
 */
public class MeasureModel {

    TrackUI ui;
    AndroidNTService androidNTService;
    private Track track;


    public MeasureModel(TrackUI ui) {
        this.ui = ui;

        androidNTService = AndroidNTService.getInstance();
        track = androidNTService.getTrack();
    }

    public void startMeasuring() {

        track = androidNTService.newTrack();
        track.addTrackUIListener(ui);
        track.start(); // this method later invoke "measuringStarted" of TrackUI
    }

    public boolean togglePause() {

        boolean isPaused = false;
        if (track != null) {
            isPaused = track.isPaused();
            if (isPaused)
                track.resume();
            else
                track.pause();
        }
        return isPaused;
    }


    public void pauseMeasuring() {
        //"pre-stop" pause (only stops AudioComponent)
        if (track != null) {
            track.pause(true);
        }
    }

    public void resumeMeasuring() {
        if (track != null && track.isPaused()) {
            track.resume();
        }
    }

    /**
     * Assumes there is a track and it is running (not checked here but at all calls)
     */
    public void stopMeasuring() {

        if (track != null) {
            track.pause(true);
            track.stop();
            androidNTService.resetTrack();
            track.removeTrackUIListener(ui);


        }
    }


    public void unregisterListener() {
        if (track != null)
            track.removeTrackUIListener(ui);
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
