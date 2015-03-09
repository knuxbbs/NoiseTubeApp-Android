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

import net.noisetube.api.NTClient;
import net.noisetube.api.Pausable;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.model.Saveable;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.SaverUI;
import net.noisetube.api.util.Logger;

import java.util.List;

/**
 * NoiseTube track and measurement saving class (abstract)
 *
 * @author mstevens, humberto
 */
public abstract class Saver implements Pausable {

    protected Logger log = Logger.getInstance();
    protected NTClient ntClient;
    protected Preferences preferences;

    protected volatile boolean running = false;
    protected volatile boolean paused = false;
    protected SaverUI ui;
    protected Track track;
    private volatile String status;

    public Saver(Track track) {
        this.track = track;
        ntClient = NTClient.getInstance();
        preferences = NTClient.getInstance().getPreferences();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public abstract void save(Saveable saveable);

    protected abstract void enableBatchMode();

    public void saveBatch(List<Saveable> savables) {
        enableBatchMode(); // !!!
        for (Saveable saveable : savables) {
            save(saveable);
        }

    }

    public void setTrack(Track track) {
        if (isRunning())
            throw new IllegalStateException(
                    "Cannot replace track while saver is still running");
        this.track = track;
    }

    public void setUI(SaverUI ui) {
        this.ui = ui;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    protected void setStatus(String message) {
        status = message;
        if (ui != null)
            ui.updated(this, message);
    }

}
