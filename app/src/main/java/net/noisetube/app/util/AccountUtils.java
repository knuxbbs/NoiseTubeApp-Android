/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.noisetube.app.util;

import android.text.TextUtils;

import net.noisetube.api.exception.AuthenticationException;
import net.noisetube.api.io.NTWebAPI;


public class AccountUtils {

    public static boolean validateCredentials(String userName, String userPass) {

        if (userName.isEmpty() || userPass.isEmpty() || userName.length() < 3 || userPass.length() < 6 || hasNoValidCharacters(userName) || hasNoValidCharacters(userPass)) {
            return false;
        }
        return true;
    }

    public static boolean validateUser(String userName) {

        if (userName.isEmpty() || userName.length() < 3 || hasNoValidCharacters(userName)) {
            return false;
        }
        return true;
    }

    public static boolean isValidHometown(String homeTown) {

        String[] values = homeTown.split(",");
        if (values.length < 2 || values[0].isEmpty() || values[0].length() < 2 || values[1].isEmpty() || values[1].length() < 2) {
            return false;
        }

        return true;
    }


    public static boolean isUserNameAvailable(String user) throws AuthenticationException {
        NTWebAPI ntWebAPI = new NTWebAPI();
        boolean response = ntWebAPI.checkUsername(user);

        return response;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean validatePassword(String userPass) {

        if (userPass.isEmpty() || userPass.length() < 6 || hasNoValidCharacters(userPass)) {
            return false;
        }
        return true;
    }

    private static boolean hasNoValidCharacters(String value) {

        final char[] charValues = value.toCharArray();
        final String otherValidChars = ".-_@";

        for (char i : charValues) {
            if (Character.isLetter(i) || Character.isDigit(i) || otherValidChars.contains(new Character(i).toString())) {
                continue;
            } else {
                return true;
            }
        }

        return false;
    }


}
