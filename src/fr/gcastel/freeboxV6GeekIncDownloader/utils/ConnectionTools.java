/*
   Copyright 2013 Gerben CASTEL

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package fr.gcastel.freeboxV6GeekIncDownloader.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Méthodes utilitaires dédiées à la connexion réseau
 *
 * @author Gerben Castel
 */
public class ConnectionTools {

    private ConnectionTools() {
        throw new AssertionError();
    }

    public static boolean isConnectedViaWifi(Context context) {
        boolean result = false;
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            result = (info != null) && (info.getType() == ConnectivityManager.TYPE_WIFI);
        }
        return result;
    }
}