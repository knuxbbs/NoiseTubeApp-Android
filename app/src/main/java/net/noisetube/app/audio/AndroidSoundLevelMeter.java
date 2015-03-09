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

import net.noisetube.api.audio.SoundLevelMeter;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.audio.recording.AudioSpecification;
import net.noisetube.api.model.MeasurementListener;
import net.noisetube.api.model.SLMMeasurement;

/**
 * @author sbarthol, mstevens
 *         <p/>
 *         Adds frequency spectrum analysis (prototype implementation)
 */
public class AndroidSoundLevelMeter extends SoundLevelMeter {

    int loop = 0;
    //private KJFFT fastFourierTransformer;


    public AndroidSoundLevelMeter(AudioSpecification audioSpec, MeasurementListener listener) throws Exception {
        super(audioSpec, null, listener);
    }

    public AndroidSoundLevelMeter(AudioSpecification audioSpec, Calibration calibration, MeasurementListener listener) throws Exception {
        super(audioSpec, calibration, listener);
    }

    @Override
    protected void analyseSamples(double[] samples, SLMMeasurement measurement) throws Exception {
        //SPECTRUM
        //We reduce the recorded samples to 100 samples and then store them
        //measurement.setReducedSamples(reduceSamples(samples));
        //TODO move actual analysis over here (it's in the GUI code now)

        //LOUDNESS (we do this last because A-filtering changes the sample values):
        super.analyseSamples(samples, measurement);
    }


}
