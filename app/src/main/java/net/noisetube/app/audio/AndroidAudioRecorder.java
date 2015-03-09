/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2012
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

package net.noisetube.app.audio;

import android.media.AudioRecord;

import net.noisetube.api.audio.AudioStreamListener;
import net.noisetube.api.audio.recording.AudioRecorder;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.audio.recording.AudioStream;
import net.noisetube.api.util.Logger;

/**
 * @author sbarthol, mstevens
 *         <p/>
 *         TODO fix AudioRecord initialization bug triggered by SLM lib
 *         Possible clues:
 *         - http://stackoverflow.com/questions/4843739/audiorecord-object-not-initializing
 *         - http://stackoverflow.com/questions/4807428/audiorecord-could-not-get-audio-input-for-record-source-1
 *         - http://stackoverflow.com/questions/4525206/android-audiorecord-class-process-live-mic-audio-quickly-set-up-callback-func
 *         - http://stackoverflow.com/questions/8233235/android-audiorecord-instance-fails
 */
public class AndroidAudioRecorder extends AudioRecorder {

    protected Logger log = Logger.getInstance();
    private AudioRecord audioRecord; //!!!
    private int bufferSize;
    private byte[] byteBuffer = null;

    public AndroidAudioRecorder(AudioSpecification audioSpec, int recordTimeMS, AudioStreamListener listener) {
        super(audioSpec, recordTimeMS, listener); //!!!
        //Calculate the bufferSize, depending on actualRecordTimeMS:
        int minBufferSize = AudioRecord.getMinBufferSize(audioSpec.getSampleRate(),
                ((AndroidAudioSpecification) audioSpec).getChannelConfig(),
                ((AndroidAudioSpecification) audioSpec).getAudioFormat());
        if (minBufferSize < 0)
            audioRecordErrorCheck("getMinBufferSize()", minBufferSize);  //throws exception!
        this.bufferSize = Math.max(audioSpec.getByteRate() * (actualRecordTimeMS / 1000), minBufferSize);
    }

    @Override
    public void release() {
        if (audioRecord != null) {
            //Stop recording
            try {
                audioRecord.stop();
            } catch (Exception e) {
                //this often fails when application is stopping (nothing to worry about)
                log.error(e, "release method");
            }
            //Release native record object
            audioRecord.release();
            audioRecord = null;
        }
    }

    /**
     * Using the AudioRecord class, we have to implement this in a different
     * way than implementing e.g. JavaMERecordTask. An AudioRecord instance
     * will be polled to read a number of bytes, in stead of reading to a
     * stream. So it is our task to make sure we ask exact the amount of bytes
     * to cover the length of the track, asked by recordTimeMS.
     */
    @Override
    protected AudioStream record() throws Exception {

        if (audioRecord == null || audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
            throw new Exception("AudioRecord not initialized");

        //Create buffer:
        /* byteBuffer is a buffer which will be filled with the recorded bytes */
        byteBuffer = new byte[bufferSize];

        //Note current time:
        long recordStartTime = System.currentTimeMillis();

        //Read bytes:
        /* The resulting bytesRead states how much bytes are actually read,
         * which should be the same as the bufferSize. It also contains
		 * errors (if any have occurred). */
        int bytesRead = audioRecord.read(byteBuffer, 0, bufferSize);
        //TODO check it we are not reading more than needed for 1s of audio, or can there be overlap? Don't we have to flush the buffer somehow?
        // maybe do this instead?: audioRecord.read(byteBuffer, 0, audioSpec.getByteRate() * (recordTimeMS / 1000))

        //Check for errors (can throw exception!):
        audioRecordErrorCheck("read()", bytesRead);

        //Check if there are as many bytes read as their should be read:
        if (bytesRead != bufferSize)
            return null;

        //Return the headerless bytestream, which will be placed in a RawAudioStream package:
        return AudioStream.packageInSuitableStream(audioSpec, recordStartTime, byteBuffer);
    }

    /**
     * Used to initiate audio recorder, which happens if it is the first time that audio is recorded.
     * Note that this is used to prepare everything for a continuous stream of audio,
     * as required when starting the application or when resuming it after it paused.
     */
    private void startRecorder() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        //Create AudioRecord:
        audioRecord = ((AndroidAudioSpecification) audioSpec).getAudioRecord(this.bufferSize);
        audioRecord.startRecording();
        /* Set thread priority to high */
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    }

    @Override
    protected AudioStream getRecordedStream() throws Exception {
        //If first time recording -> recorder not yet started -> initiate recorder
        if (audioRecord == null || audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING)
            startRecorder();
        return super.getRecordedStream();
        //DO NOT STOP RECORDING HERE!
    }

    @Override
    public boolean testRecord() throws Exception {
        try {
            return super.testRecord();
        } finally //!!!
        {
            if (audioRecord != null) {
                audioRecord.stop();
            }
        }
    }

    private void audioRecordErrorCheck(String method, int returnCode) throws IllegalStateException {
        //Check for errors:
        switch (returnCode) {
            case AudioRecord.ERROR_INVALID_OPERATION:
                throw new IllegalStateException("AudioRecord." + method + " returned AudioRecord.ERROR_INVALID_OPERATION");
            case AudioRecord.ERROR_BAD_VALUE:
                throw new IllegalStateException("AudioRecord." + method + " returned AudioRecord.ERROR_BAD_VALUE");
            case AudioRecord.ERROR:
                throw new IllegalStateException("AudioRecord." + method + " returned AudioRecord.ERROR");
        }
    }

}
