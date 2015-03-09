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

package net.noisetube.api.tagging;

import net.noisetube.api.util.StringUtils;

import java.util.ArrayList;

/**
 * @author mstevens, humberto
 */
public final class TagsHelper {

    public static final char TAG_SEPARATOR_FOR_SAVING = ',';


    private TagsHelper() {
    } // class should not be instantiated

    /**
     * Makes all tags lowercase and concatinates them using comma's as separator
     *
     * @param tags
     * @return savable tagsstring
     */
    public static String getSavableTagsString(ArrayList<String> tags) {
        return getTagsString(tags, TAG_SEPARATOR_FOR_SAVING).toLowerCase();
    }

    public static String getTagsString(ArrayList<String> tags, char separator) {
        if (tags == null)
            return "";

        StringBuilder bff = new StringBuilder();
        final int size = tags.size() - 1;
        for (int i = 0; i < size; i++) {
            bff.append(tags.get(i));
            bff.append(TAG_SEPARATOR_FOR_SAVING);
        }
        bff.append(tags.get(size));

        return bff.toString();
    }

    public static String[] splitTypedTags(String tagsString) {
        return StringUtils.split(tagsString.trim(),
                new char[]{','});
    }

}
