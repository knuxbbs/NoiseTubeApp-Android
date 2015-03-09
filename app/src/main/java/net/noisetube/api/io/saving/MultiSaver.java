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

package net.noisetube.api.io.saving;

import net.noisetube.api.model.Saveable;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.SaverUI;

import java.util.ArrayList;
import java.util.List;


/**
 * Multiplexing saver class
 *
 * @author mstevens, humberto
 */
public class MultiSaver extends Saver implements SaverUI {

    private List<Saver> savers;

    public MultiSaver(Track track) {
        super(track);
        savers = new ArrayList<Saver>();
    }

    public void addSaver(Saver saver) {
        savers.add(saver);
        saver.setUI(this);
    }

    /**
     * @see net.noisetube.api.io.saving.Saver#save(net.noisetube.api.model.Saveable)
     */
    @Override
    public void save(Saveable saveable) {
        // don't check for paused here, otherwise taggedintervals are not saved & last
        // pre-paused measurement may be lost as well(?)

        if (running) {
            for (Saver saver : savers) {
                saver.save(saveable);
            }
        }
    }

    /**
     * @see net.noisetube.api.Startable#start()
     */
    public void start() {
        if (!running) {
            for (Saver saver : savers) {
                saver.start();
            }

            running = true;
            paused = false;
        }
    }

    public void pause() {
        if (running) {
            for (Saver saver : savers) {
                saver.pause();
            }
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            for (Saver saver : savers) {
                saver.resume();
            }
            paused = false; // resume from pause
        }
    }

    /**
     * @see net.noisetube.api.Startable#stop()
     */
    public void stop() {
        if (running) {
            running = false;
            paused = false;
            for (Saver saver : savers) {
                saver.stop();
            }
        }
    }

    /**
     * @see
     */
    public boolean isRunning() {
        if (this.running)
            return true;
        else {
            // if multisaver not running, check if underlying servers are also
            // stopped
            for (Saver saver : savers) {
                if (saver.isRunning()) {
                    return true; // one saver running -> multisaver is running
                }

            }
            return false;
        }
    }

    public void enableBatchMode() {
        if (running) {
            for (Saver saver : savers) {
                saver.enableBatchMode();
            }
        }
    }

    public String getStatus() {
        StringBuilder bff = new StringBuilder();
        for (Saver saver : savers) {
            bff.append(" - " + saver.getStatus()).append("\n");
        }

        return bff.toString();
    }

    public void updated(Saver saver, String message) {
        if (ui != null)
            ui.updated(this, getStatus());
    }

    public void setTrack(Track track) {
        super.setTrack(track);
        for (Saver saver : savers) {
            saver.setTrack(track);
        }
    }
}