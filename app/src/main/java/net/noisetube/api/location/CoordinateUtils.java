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

package net.noisetube.api.location;

import net.noisetube.api.NTClient;
import net.noisetube.api.model.NTCoordinates;
import net.noisetube.api.util.MathNT;

/**
 * @author mstevens
 *         <p/>
 *         Translate coordinates algorithm ported from JavaScript version made available as part
 *         of the "Great Circle Calculator" by Ed Williams:
 *         <a href="http://williams.best.vwh.net/gccalc.htm">http://williams.best.vwh.net/gccalc.htm</a>
 */
public class CoordinateUtils {

    public static NTCoordinates translate(NTCoordinates startingPoint, double courseInDegrees, double distanceInMeter) throws Exception {
        double glat1 = Math.toRadians(startingPoint.getLatitude()); //initial geodetic latitude in radians N positive
        double glon1 = Math.toRadians(startingPoint.getLongitude()); //initial geodetic longitude in radians E positive

        double faz = Math.toRadians(courseInDegrees); // faz forward azimuth in radians
        double s = distanceInMeter / 1852.0d; // s distance in units of a (=nm)

        double EPS = 0.00000000005d;
        double r, tu, sf, cf, b, cu, su, sa, c2a, x, c, d, y, sy = 0.0d, cy = 0.0d, cz = 0.0d, e = 0.0d;
        double glat2, glon2;
        if ((Math.abs(Math.cos(glat1)) < EPS) && !(Math.abs(Math.sin(faz)) < EPS))
            throw new Exception("Only N-S courses are meaningful, starting at a pole!");
        double a = WGS84Ellipsoid.getA();
        double f = WGS84Ellipsoid.getF();
        r = 1 - f;
        tu = r * Math.tan(glat1);
        sf = Math.sin(faz);
        cf = Math.cos(faz);
        if (cf == 0)
            b = 0.d;
        else
            b = 2.d * atan2(tu, cf);
        cu = 1.d / Math.sqrt(1d + tu * tu);
        su = tu * cu;
        sa = cu * sf;
        c2a = 1 - sa * sa;
        x = 1.d + Math.sqrt(1.d + c2a * (1.d / (r * r) - 1.d));
        x = (x - 2.d) / x;
        c = 1.d - x;
        c = (x * x / 4.d + 1.d) / c;
        d = (0.375d * x * x - 1.d) * x;
        tu = s / (r * a * c);
        y = tu;
        c = y + 1;
        while (Math.abs(y - c) > EPS) {
            sy = Math.sin(y);
            cy = Math.cos(y);
            cz = Math.cos(b + y);
            e = 2.d * cz * cz - 1.d;
            c = y;
            x = e * cy;
            y = e + e - 1.d;
            y = (((sy * sy * 4.d - 3.d) * y * cz * d / 6.d + x) * d / 4.d - cz) * sy * d + tu;
        }
        b = cu * cy * cf - su * sy;
        c = r * Math.sqrt(sa * sa + b * b);
        d = su * cy + cu * sy * cf;
        glat2 = modlat(atan2(d, c));
        c = cu * cy - su * sy * cf;
        x = atan2(sy * sf, c);
        c = ((-3.d * c2a + 4.d) * f + 4.d) * c2a * f / 16.d;
        d = ((e * cy * c + cz) * sy * c + y) * sa;
        glon2 = modlon(glon1 + x - (1.d - c) * d * f); //fix date line problems

        return NTClient.getInstance().getNTCoordinates(Math.toDegrees(glat2), Math.toDegrees(glon2), startingPoint.getAltitude());
    }

    /**
     * mstevens: We could move this to MathNT (and use native Math.atan2 on Android), but I'm not 100% sure the result is the same in all cases
     */
    private static double atan2(double y, double x) throws Exception {
        double out = 0.;
        if (x < 0)
            out = MathNT.atan(y / x) + Math.PI;
        if ((x > 0) && (y >= 0))
            out = MathNT.atan(y / x);
        if ((x > 0) && (y < 0))
            out = MathNT.atan(y / x) + 2 * Math.PI;
        if ((x == 0) && (y > 0))
            out = Math.PI / 2;
        if ((x == 0) && (y < 0))
            out = 3 * Math.PI / 2;
        if ((x == 0) && (y == 0))
            throw new Exception("atan2(0,0) undefined");
        return out;
    }

    private static double modlat(double x) {
        return ((x + Math.PI / 2) % (2 * Math.PI)) - Math.PI / 2;
    }

    private static double modlon(double x) {
        return ((x + Math.PI) % (2 * Math.PI)) - Math.PI;
    }

    public static class WGS84Ellipsoid {

        private static final double a = (6378.137d / 1.852d);
        private static final double invf = 298.257223563d;

        public static double getA() {
            return a;
        }

        public static double getInvF() {
            return invf;
        }

        public static double getF() {
            return 1.d / getInvF();
        }
    }

}
