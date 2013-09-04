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

import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Le service de découverte de la freebox
 * Pour l'instant, on se repose sur l'adresse "mafreebox.freebox.fr".
 * Quand la base installée aura évoluée, il faudra effectuer une découverte réseau
 * avec l'API Android 4.1 NsdManager.DiscoveryListener.
 *
 * @author Gerben Castel
 */
public class FreeboxDiscovery {

    private FreeboxDiscovery() {
        throw new AssertionError();
    }

    /**
     * Lance la recherche d'une freebox et retourne le code JSON
     * obtenu
     *
     * Ex : {"uid":"598663ccd3b3b02e13d580524066d54c","device_name":"Freebox Server","api_version":"1.0","api_base_url":"\/api\/","device_type":"FreeboxServer1,1"}
     *
     * @return la chaîne JSON obtenue lors de la détection ou null si non trouvée
     */
    public static String findFreebox() {
        Log.d("[FreeboxDiscovery]", "Recherche d'une freebox");
        HttpGet getReq = new HttpGet("http://mafreebox.freebox.fr/api_version");

        return NetworkTools.executeHTTPRequest("[FreeboxDiscovery]", getReq, null);
    }

    public static String findFreeboxAPIURL()  {
        String freeboxDiscoveryString = FreeboxDiscovery.findFreebox();
        if (freeboxDiscoveryString != null) {
            Log.d("[FreeboxDiscovery]", "Résultat discovery : " + freeboxDiscoveryString);
            JSONObject jObject;
            String urlFreebox;
            try {
                jObject = new JSONObject(freeboxDiscoveryString);

                urlFreebox = "http://mafreebox.freebox.fr" +
                        jObject.getString("api_base_url") +
                        "v" + jObject.getString("api_version").replace(".0","");
            } catch (JSONException e) {
                Log.e("[FreeboxDiscovery]", "Erreur de parsing JSON: ",e);
                return null;
            }
            Log.d("[FreeboxDiscovery]", "L'URL de l'API freebox est donc : " + urlFreebox);
            return urlFreebox;
        } else {
            Log.d("[FreeboxDiscovery]", "Freebox non trouvée");
            return null;
        }
    }
}
