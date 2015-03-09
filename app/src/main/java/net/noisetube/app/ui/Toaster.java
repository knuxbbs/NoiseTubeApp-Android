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

package net.noisetube.app.ui;


import android.app.Activity;
import android.widget.Toast;

import net.noisetube.api.NTClient;
import net.noisetube.api.util.Logger;
import net.noisetube.app.core.AndroidNTClient;

/**
 * This class can be used to create small pop-up screens which are only displayed for a few seconds.
 * These are also called toasts and are used to display e.g. a confirmation (like "status saved").
 *
 * @author sbarthol, humberto
 */
public class Toaster extends Activity {

    static public Toaster instance = new Toaster();

    private static Logger log = Logger.getInstance();

    public static void displayToast(String text) {
        try {
            Toast.makeText(((AndroidNTClient) NTClient.getInstance()).getContextWrapper(), text, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            log.error(e, "displayToast");
        }

    }

    public static void displayToast(String text, int gravity) {
        try {

            Toast toast = Toast.makeText(((AndroidNTClient) NTClient.getInstance()).getContextWrapper(), text, Toast.LENGTH_LONG);
            toast.setGravity(gravity, 0, 0);
            toast.show();
        } catch (Exception e) {
            log.error(e, "displayToast");
        }

    }

    public static void displayShortToast(String text) {
        try {
            Toast.makeText(((AndroidNTClient) NTClient.getInstance()).getContextWrapper(), text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            log.error(e, "displayShortToast");
        }

    }

    public static void displayShortToast(String text, int gravity) {

        try {
            Toast toast = Toast.makeText(((AndroidNTClient) NTClient.getInstance()).getContextWrapper(), text, Toast.LENGTH_SHORT);
            toast.setGravity(gravity, 0, 0);
            toast.show();
        } catch (Exception e) {
            log.error(e, "displayShortToast");
        }

    }
}
