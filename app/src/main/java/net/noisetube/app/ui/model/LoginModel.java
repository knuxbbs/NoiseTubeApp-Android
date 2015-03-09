package net.noisetube.app.ui.model;

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


import net.noisetube.api.NTClient;
import net.noisetube.api.config.NTAccount;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.io.NTWebAPI;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;


/**
 * @author humberto
 */
public final class LoginModel {

    public static final int LOGIN_OK = 1;
    public static final int LOGIN_FAILED = 2;
    public static final int CONNECTION_EXCEPTION = 3;
    private AndroidPreferences preferences;
    private NTWebAPI api;
    private Logger log;

    public LoginModel() {
        log = Logger.getInstance();
        preferences = AndroidPreferences.getInstance();
        api = new NTWebAPI();

    }

    public int login(String user, String pass) {

        NTAccount account = null;
        try {
            account = api.login(user, pass);


            if (account != null) {
                log.debug("Logged in (API key: " + account.getAPIKey() + ")");

                preferences.setAccount(account);

                int savingMode = NTClient.getInstance().getFavoriteSavingMode();

                preferences.setSavingModeAndPersist(savingMode);
                return LOGIN_OK;
            } else {
                return LOGIN_FAILED;
            }
        } catch (Exception e) {
            log.error(e, "Error upon authentication");
            preferences.setSavingMode(NTClient.getInstance().getDevice().supportsFileAccess() ? Preferences.SAVE_FILE : Preferences.SAVE_NO);
            return CONNECTION_EXCEPTION;
        }
    }

    public void updateAppPreferences() {
        preferences.markSkippedSignIn();
        preferences.setSavingModeAndPersist(NTClient.getInstance().getDevice().supportsFileAccess() ? Preferences.SAVE_FILE : Preferences.SAVE_NO);
    }

    public int getSavingMode() {
        return preferences.getSavingMode();
    }
}
