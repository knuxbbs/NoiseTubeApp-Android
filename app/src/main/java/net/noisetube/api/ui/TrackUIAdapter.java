package net.noisetube.api.ui;

import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Track;

/**
 * @author Humberto
 */
public abstract class TrackUIAdapter implements TrackUI {
    @Override
    public void newMeasurement(Track track, NTMeasurement newMeasurement, NTMeasurement savedMeasurement) {

    }

    @Override
    public void measuringStarted(Track track) {

    }

    @Override
    public void measuringPaused(Track track) {

    }

    @Override
    public void measuringResumed(Track track) {

    }

    @Override
    public void measuringStopped(Track track) {

    }
}
