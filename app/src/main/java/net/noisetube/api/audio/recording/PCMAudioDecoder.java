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

package net.noisetube.api.audio.recording;

import net.noisetube.api.util.MathNT;

/**
 * @author mstevens
 */
public class PCMAudioDecoder extends AudioDecoder {

    /**
     *
     */
    private static final long serialVersionUID = -9221914775628601548L;

    /**
     * @param audiostream to decode
     * @param sampleIndex in interval [0, numSamples[
     * @param channel     numbered 0, 1, ...
     * @return offset (within the rawData byte array) of the first byte of the sample with number <sampleIndex> for channel <channel>
     * <p/>
     * TODO channel position may not be correct for 24bits PCM, 32/64bit floating point PCM or multi-channel files
     */
    private int getSampleAndChannelOffset(AudioStream audioStream, int sampleIndex, int channel) {
        if (channel < 0 || channel >= audioStream.numChannels)
            throw new IllegalArgumentException("Invalid channel, should be 0 <= channel < " + audioStream.numChannels);
        return audioStream.getSampleOffset(sampleIndex) + (channel * audioStream.bitsPerSample / 8); /*point to correct channel*/
    }

    /**
     * @param audiostream to decode
     * @param sampleIndex in interval [0, numSamples[
     * @param channel     numbered 0, 1, ...
     * @return the wave amplitude at/for this sample as a double precision integer from the [-(2^(bitsPerSample-1)); 2^(bitsPerSample-1)-1] interval
     * <p/>
     * TODO 24bits PCM or 32/64bit floating point PCM, non-PCM content
     */
    public long decodeSampleInteger(AudioStream audioStream, int sampleIndex, int channel) {
        int startByteOffset = getSampleAndChannelOffset(audioStream, sampleIndex, channel);
        long result = 0;
        if (audioStream.bitsPerSample == 8 || audioStream.bitsPerSample == 16 || audioStream.bitsPerSample == 24) //integer format
        {
            switch (audioStream.bitsPerSample) {
                //8bit (normally unsigned)
                case 8:
                    if (audioStream.signed == AudioFormat.SIGNED)
                        result = audioStream.readByte(startByteOffset); //normally not the case and don't know how to detect it (readHeader of WAVEAudioStream sets signed=false upon bitsPerSample=8)
                    else //UNSIGNED
                        result = audioStream.readByte(startByteOffset) - 128; //put the sample in the [-128; 127] interval
                    break;
                //16bit (always signed)
                case 16:
                    result = audioStream.readSignedShort(startByteOffset); //[-32768; 32767] interval
                    break;
                //TODO 24bit
            }
        } else if (audioStream.bitsPerSample == 32 || audioStream.bitsPerSample == 64) //floating point format
        {    //TODO this is untested
            double fpSample = (long) decodeSampleFloating(audioStream, sampleIndex, channel);
            switch (audioStream.bitsPerSample) {
                //32bit
                case 32:
                    result = MathNT.round(fpSample * (fpSample < 0 ? (Integer.MIN_VALUE * -1.0d) : Integer.MAX_VALUE));
                    break;
                //64bit
                case 64:
                    result = MathNT.round(fpSample * (fpSample < 0 ? (Long.MIN_VALUE * -1.0d) : Long.MAX_VALUE));
                    break;
            }
        }
        //else //unsupported/doesn't exist
        return result;
    }

    /**
     * @param audiostream to decode
     * @param sampleIndex
     * @param channel     numbered 0, 1, ...
     * @return the wave amplitude at/for this sample as a double precision floating point number in the interval [-1.0; 1.0]
     * <p/>
     * TODO 24bits PCM or 32/64bit floating point PCM, non-PCM content
     */
    public double decodeSampleFloating(AudioStream audioStream, int sampleIndex, int channel) {
        double result = 0;
        if (audioStream.bitsPerSample == 8 || audioStream.bitsPerSample == 16 || audioStream.bitsPerSample == 24) //integer format
        {
            long iSample = decodeSampleInteger(audioStream, sampleIndex, channel);
            //Convert to floating point
            switch (audioStream.bitsPerSample) {
                //8bit
                case 8:
                    result = (double) iSample / (iSample < 0 ? 128 : 127);
                    break;
                //16bit
                case 16:
                    result = (double) iSample / (iSample < 0 ? (Short.MIN_VALUE * -1.0d) : Short.MAX_VALUE);
                    break;
                //TODO 24bit
            }
        } else if (audioStream.bitsPerSample == 32 || audioStream.bitsPerSample == 64) //floating point format
        {
            //TODO 32/64 bit floating point format
        }
        //else //unsupported/doesn't exist
        return result;
    }

}
