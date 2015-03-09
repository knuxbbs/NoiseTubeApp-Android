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

package net.noisetube.api.config;

import android.graphics.Bitmap;

import net.noisetube.app.util.ImageUtils;

public class NTAccount {

    private static final String separator = "#";

    private String APIKey;
    private String username;
    private Bitmap avatar;

    public NTAccount(String username, String APIKey) {
        this.APIKey = APIKey;
        this.username = username;
        this.avatar = null;

    }

    public NTAccount(String username, String APIKey, Bitmap avatar) {
        this.APIKey = APIKey;
        this.username = username;
        this.avatar = avatar;

    }

    public static NTAccount deserialise(String serialisedAccount) {
        if (serialisedAccount == null || serialisedAccount.equals("") || serialisedAccount.indexOf(separator) == -1)
            throw new IllegalArgumentException("serialisedAccount is invalid");
        String[] tokens = serialisedAccount.split(separator);

        Bitmap avatar = ImageUtils.loadImageFromStorage();

        return new NTAccount(tokens[0], tokens[1], avatar);
    }

    /**
     * @return the aPIKey
     */
    public String getAPIKey() {
        return APIKey;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    public String serialise() {
        return username + separator + APIKey;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

}
