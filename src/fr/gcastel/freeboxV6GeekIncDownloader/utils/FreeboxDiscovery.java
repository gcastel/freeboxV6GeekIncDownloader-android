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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        String result = "";
        HttpGet getReq = new HttpGet("http://mafreebox.freebox.fr/api_version");
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(getReq);

            // Trouvé ?
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream content = response.getEntity().getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(content));
                String line = br.readLine();

                while (line != null) {
                    result += line;
                    line = br.readLine();
                }

                br.close();
                content.close();
            } else {
                Log.d("[FreeboxDiscovery]", "Freebox non trouvée, status code : "  + response.getStatusLine().getStatusCode());
                result = null;
            }
        } catch (Exception e) {
            Log.d("[FreeboxDiscovery]", "Network exception", e);
            result = null;
        }

        return result;
    }
}