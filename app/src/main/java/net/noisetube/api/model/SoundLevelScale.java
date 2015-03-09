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

package net.noisetube.api.model;

import net.noisetube.api.ui.NTColor;

/**
 * @author mstevens
 */
public final class SoundLevelScale {

    static public final int SOUNDLEVEL_VERY_HIGH = 0;
    static public final int SOUNDLEVEL_HIGH = 1;
    static public final int SOUNDLEVEL_NORMAL = 2;
    static public final int SOUNDLEVEL_LOW = 3;

    static public final float SOUNDLEVEL_BOUNDARY_VERY_HIGH = 80;
    static public final float SOUNDLEVEL_BOUNDARY_HIGH = 70;
    static public final float SOUNDLEVEL_BOUNDARY_NORMAL = 60;
    static public final float SOUNDLEVEL_BOUNDARY_LOW = 50;

    public SoundLevelScale() {
    } //no-one should instantiate this class

    public static int getSoundLevelCategory(double db) {
        if (db >= SOUNDLEVEL_BOUNDARY_VERY_HIGH)
            return SOUNDLEVEL_VERY_HIGH;
        if (db >= SOUNDLEVEL_BOUNDARY_HIGH)
            return SOUNDLEVEL_HIGH;
        if (db >= SOUNDLEVEL_BOUNDARY_NORMAL)
            return SOUNDLEVEL_NORMAL;
        return SOUNDLEVEL_LOW;
    }

    static public NTColor getColor(double db) {
        return getColorForCategory(getSoundLevelCategory(db));
    }

    static public NTColor getColorForCategory(int category) {
        switch (category) {
            case SOUNDLEVEL_VERY_HIGH:
                return new NTColor(221, 44, 0); //red
            case SOUNDLEVEL_HIGH:
                return new NTColor(255, 152, 0); //orange
            case SOUNDLEVEL_NORMAL:
                return new NTColor(255, 235, 59); //yellow
            case SOUNDLEVEL_LOW:
                return new NTColor(76, 175, 80); //green
            default:
                return new NTColor(0xFF, 0xFF, 0xFF); //white
        }

    }

    public static NTColor getNoiseMapColor(double db) {
        return getNoiseMapColorForCategory(getSoundLevelCategory(db));
    }

    public static NTColor getNoiseMapColorForCategory(int category) {
        switch (category) {
            case SOUNDLEVEL_VERY_HIGH:
                return new NTColor(255, 82, 82); //red
            case SOUNDLEVEL_HIGH:
                return new NTColor(255, 193, 7); //orange
            case SOUNDLEVEL_NORMAL:
                return new NTColor(255, 235, 59); //yellow
            case SOUNDLEVEL_LOW:
                return new NTColor(205, 220, 57); //green
            default:
                return new NTColor(0xFF, 0xFF, 0xFF); //white
        }

    }

}
