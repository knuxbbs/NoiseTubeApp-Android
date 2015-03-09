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

import java.io.Serializable;

/**
 * @author mstevens
 */
public abstract class AudioDecoder implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3692715809206878728L;

    //STATICS:
    public static AudioDecoder getAudioDecoderFor(int encoding) throws Exception {
        AudioDecoder decoder = null;
        switch (encoding) {
            case AudioFormat.ENCODING_LINEAR_PCM:
                decoder = new PCMAudioDecoder();
                break;
            //...
            case AudioFormat.NOT_SPECIFIED:    //log.debug("Cannot decide in decoder (no encoding specified)");
                break; //method will return null
            default:
                throw new Exception("Unsupported encoding (no suitable decoder class)");
        }
        return decoder;
    }

    /**
     * @param audiostream to decode
     * @param sampleIndex in interval [0, numSamples[
     * @param channel     numbered 0, 1, ...
     * @return the wave amplitude at/for this sample as a double precision integer in the [-(2^(bitsPerSample-1)); 2^(bitsPerSample-1)-1] interval
     */
    public abstract long decodeSampleInteger(AudioStream audioStream, int sampleIndex, int channel);

    public long[] decodeSamplesInteger(AudioStream audioStream, int start, int end, int channel) {
        if (end < start)
            throw new IllegalArgumentException("Invalid sample range: end before start");
        long[] samples = new long[end - start + 1];
        for (int i = 0; i < samples.length; i++)
            samples[i] = decodeSampleInteger(audioStream, start + i, channel);
        return samples;
    }

    /**
     * @param audiostream to decode
     * @param sampleIndex
     * @param channel     numbered 0, 1, ...
     * @return the wave amplitude at/for this sample as a double precision floating point number in the interval [-1.0; 1.0]
     */
    public abstract double decodeSampleFloating(AudioStream audioStream, int sampleIndex, int channel);

    public double[] decodeSamplesFloating(AudioStream audioStream, int start, int end, int channel) {
        if (end < start)
            throw new IllegalArgumentException("Invalid sample range: end before start");
        double[] samples = new double[end - start + 1];
        for (int i = 0; i < samples.length; i++)
            samples[i] = decodeSampleFloating(audioStream, start + i, channel);
        return samples;
    }

}
