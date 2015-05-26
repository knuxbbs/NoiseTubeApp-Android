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

package net.noisetube.api.audio;

import net.noisetube.api.Pausable;
import net.noisetube.api.SLMClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.audio.calibration.Corrector;
import net.noisetube.api.audio.recording.AudioDecoder;
import net.noisetube.api.audio.recording.AudioRecorder;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.audio.recording.AudioStream;
import net.noisetube.api.model.MeasurementListener;
import net.noisetube.api.model.SLMMeasurement;
import net.noisetube.api.util.Logger;
import net.noisetube.api.util.MathNT;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author mstevens, maisonneuve
 *         <p/>
 *         TODO implement time-weighted Lp
 */
public class SoundLevelMeter implements Pausable, AudioStreamListener, Serializable {

    //STATICS--------------------------------------------------------
    public static final int DEFAULT_RECORDING_TIME_MS = 1000;
    protected int recordingTimeMS = DEFAULT_RECORDING_TIME_MS;
    public static final int DEFAULT_INTERVAL_MS = 1000; //interval between 2 recordings
    protected int intervalTimeMS = DEFAULT_INTERVAL_MS;
    public static final int SPL_DB_ONLY = 0;
    public static final int SPL_DBA_ONLY = 1;
    public static final int DEFAULT_SPL_DB_MODE = SPL_DBA_ONLY;
    //protected double timeWeightingS = DEFAULT_TIME_WEIGHTING_S;
    protected int dbMode = DEFAULT_SPL_DB_MODE;
    public static final int SPL_DB_AND_DBA = 2;
    public static final int RESTART_AFTER_ILLEGAL_RESULTS = 4;
    /**
     *
     */
    private static final long serialVersionUID = 4L;
    //DYNAMICS-------------------------------------------------------
    protected Logger log = Logger.getInstance();
    protected SLMClient client;
    protected AudioSpecification audioSpec;
    protected AudioRecorder recorder;
    protected AudioDecoder decoder;
    protected Filter theAFilter = null;
    protected Calibration calibration;
    protected Corrector calibrationCorrector;
    protected MeasurementListener listener;
    protected Timer timer;
    protected TimerTask task;
    protected boolean paused = false;
    private long totalTimeActive = 0; //in ms
    private long startTime = 0;
    private int illegalResults = 0;

    public SoundLevelMeter(AudioSpecification audioSpec, MeasurementListener listener) throws Exception {
        this(audioSpec, null, listener);
    }

    public SoundLevelMeter(AudioSpecification audioSpec, Calibration calibration, MeasurementListener listener) throws Exception {
        this.client = SLMClient.getInstance();
        this.audioSpec = audioSpec;
        if (dbMode > SPL_DB_ONLY) {
            if (audioSpec.isResultSampleRateSet())
                theAFilter = Filter.getFilter(Filter.FILTER_TYPE_A_WEIGHTING, (int) audioSpec.getResultSampleRate());
            else if (audioSpec.isSampleRateSet())
                theAFilter = Filter.getFilter(Filter.FILTER_TYPE_A_WEIGHTING, (int) audioSpec.getSampleRate());
            else
                throw new Exception("Cannot configure A-filter because samplerate is unknown");
        }
        this.calibration = calibration;
        this.listener = listener;
    }

    public void start() {
        if (recorder == null) //if(!running)
        {
            if (calibration != null)
                calibrationCorrector = calibration.getCorrector();
            startRecording(client.getAudioRecorder(audioSpec, recordingTimeMS, this));
            log.info("SoundLevelMeter started:");
            log.info(" - recording for " + recordingTimeMS / 1000 + "s every " + intervalTimeMS / 1000 + "s");
            log.info(" - using audio spec: " + audioSpec.toVerboseString());
            log.info(" - " + (calibration != null ? "using " + calibration.toString() : "without calibration"));
            if (calibration != null)
                log.info(" - effective calibration credibility: " + calibration.getEffeciveCredibilityIndex());
        }
    }

    //TODO check this: http://stackoverflow.com/questions/4777060/android-sample-microphone-without-recording-to-get-live-amplitude-level
    //	timer.scheduleAtFixedRate(new RecorderTask(recorder), 0, 1000);
    private void startRecording(AudioRecorder recorder) {
        timer = new Timer();
        task = recorder.getTimerTask();
        timer.schedule(task, 0, intervalTimeMS);
        startTime = System.currentTimeMillis();
        illegalResults = 0;
        this.recorder = recorder; //!!! running=true
    }

    private void stopRecording() {
        try {


            if (task != null) {
                task.cancel();
                task = null;
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
                totalTimeActive += System.currentTimeMillis() - startTime;
                startTime = 0;
                //don't do recorder=null here!!!
            }
        } catch (NullPointerException e) {
            log.error(e, "stopRecording method");
        }
    }

    public void pause() {
        if (recorder != null /*running*/ && !paused) {
            paused = true;
            stopRecording();
        }
    }

    public void resume() {
        if (recorder != null /*running*/ && paused) {
            paused = false;
            startRecording(recorder); //resume with same recorder (and same corrector)
        }
    }

    public void stop() {
        try {
            if (recorder != null) //if(running)
            {
                stopRecording();
                recorder.release();
                recorder = null; //!!! running=false
                calibrationCorrector = null;
                paused = false;
            }
        } catch (NullPointerException e) {
            log.error(e, "Exception during stop");
        }

    }

    public boolean isRunning() {
        return recorder != null;
    }

    public boolean isPaused() {
        return paused;
    }

    public long getTotalTimeActiveMS() {
        return totalTimeActive + (startTime > 0 ? System.currentTimeMillis() - startTime : 0);
    }

    public void receiveAudioStream(final AudioStream stream) {
        if (recorder != null /*running*/ && !paused) {
            try {
                if (!stream.isValid(recordingTimeMS))
                    throw new Exception("Invalid audio stream");
                if (decoder == null) {
                    if (audioSpec.getDecoder() == null)
                        audioSpec.inferResultsFrom(stream); //infer from current stream
                    decoder = audioSpec.getDecoder(); //will return null if undecodable
                    if (decoder == null)
                        throw new Exception("Undecodeable audio stream");
                }
                SLMMeasurement m = client.createMeasurement(stream.getRecordStartTime());
                analyseSamples(decoder.decodeSamplesFloating(stream, 0, (int) stream.getSampleRate() - 1, 0), //we only look at the first channel (for now)
                        m);
                listener.newMeasurement(m);
            } catch (IllegalResultException e) {
                dealWithIllegalResult(e);
            } catch (Exception e) {
                log.error(e, "Exception during audio analysis");
            }
        }
    }

    /**
     * On some Android devices (at least the HTC Desire Z) we sometimes get NaN Leq results after resuming from pause.
     * Restarting the recording seems to correct the problem.
     * Therefore this method counts the number of such illegal results and restarts recording (pause+resume) after a
     * predefined number (RESTART_AFTER_ILLEGAL_RESULTS). This cures the problem on the HTC.
     *
     * @param exception
     */
    protected void dealWithIllegalResult(IllegalResultException exception) {
        log.error("Invalid audio analysis result: " + exception.getMessage());
        if (++illegalResults >= RESTART_AFTER_ILLEGAL_RESULTS) {    //Restart:
            pause();
            resume();
            illegalResults = 0;
        }
    }

    protected void analyseSamples(double[] samples, SLMMeasurement measurement) throws Exception {
        //SOUND PRESSURE LEVEL
        if (dbMode == SPL_DB_ONLY || dbMode == SPL_DB_AND_DBA) {
            measurement.setLeqDB((calibrationCorrector != null ? calibrationCorrector.correctDB(computeLeq(samples)) : computeLeq(samples)));
        }
        if (dbMode > SPL_DB_ONLY) {    //Apply A-weighting filtering
            samples = theAFilter.apply(samples);
            measurement.setLeqDBA((calibrationCorrector != null ? calibrationCorrector.correctDBA(computeLeq(samples)) : computeLeq(samples)));
        }
    }

    /**
     * compute Leq from the array of samples
     * <p/>
     * Leq = 10*log10[(1/T)*S[0,T](pt/p0)^2)] dt
     * with S[x,y]=integral over interval [x,y]
     * and p0 = 2x10^-5 = 0,00002
     * <p/>
     * Integral computed as a Riemann sum (http://en.wikipedia.org/wiki/Riemann_integral):
     * <p/>
     * Leq = 10*log10[(1/T)*E[0,T]((pt/p0)^2)]
     * = 10*log10[(1/T)*E[0,T]((pt*(1/p0))^2)]
     * = 10*log10[(1/T)*E[0,T](pt^2*(1/p0)^2)]
     * = 10*log10[(1/T)*E[0,T](pt^2)*(1/p0)^2]
     * = 10*log10[(1/T)*E[0,T](pt^2)*(1/p0)^2]
     * = 10*[log10[(1/T)*E[0,T](pt^2)] + log10[(1/p0)^2]]
     * = 10*log10[(E[0,T](pt^2)/T] + 10*log10[(1/p0)^2]
     * = 10*log10[(E[0,T](pt^2)/T] + 20*log10[(1/p0)]
     * = 10*log10[(E[0,T](pt^2)/T] + 20*log10[50000]
     * = 10*log10[(E[0,T](pt^2)/T] + 93.97940008672037609572522210551
     * Log info: http://oakroadsystems.com/math/loglaws.htm
     *
     * @param samples : array of sound samples (double)
     * @return the Leq
     */
    private double computeLeq(double samples[]) throws Exception {
        double sumsquare = 0.0d, leq;
        for (int i = 0; i < samples.length; i++)
            sumsquare += samples[i] * samples[i];
        leq = (10.0d * MathNT.log10(sumsquare / samples.length)) + 93.97940008672037609572522210551d;
        if (Double.isNaN(leq) || leq <= 0)
            throw new IllegalResultException("Leq is NaN, negative or zero: " + Double.toString(leq));
        return leq;
    }

    /**
     * @return the dbMode
     */
    public int getSLMMode() {
        return dbMode;
    }

    /**
     * @param dbMode the dbMode to set
     */
    public void setSLMMode(int dbMode) {
        this.dbMode = dbMode;
    }

    /**
     * @return the intervalTimeMS
     */
    public int getIntervalTimeMS() {
        return intervalTimeMS;
    }

    public void setListener(MeasurementListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    /**
     * @author mstevens
     */
    private class IllegalResultException extends Exception {

        private static final long serialVersionUID = -3972402768127493264L;

        public IllegalResultException(String message) {
            super(message);
        }

    }

}