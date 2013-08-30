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
package fr.gcastel.freeboxV6GeekIncDownloader.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
public class FreeboxDiscoveryService extends AsyncTask<Void, Void, String> {

    private final Context applicationContext;

    public FreeboxDiscoveryService(Context context) {
        applicationContext = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        Log.d("[FreeboxDiscoveryService]", "Recherche d'une freebox");
        String result = null;
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
                    line = br.readLine();
                    result += line;
                }

                br.close();
                content.close();
            } else {
                Log.d("[FreeboxDiscoveryService]", "Freebox non trouvée, status code : "  + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            Log.d("[FreeboxDiscoveryService]", "Network exception", e);
            result = null;
        }
        return result;
    }

    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(applicationContext, "Résultat " + result, Toast.LENGTH_LONG).show();
        } else {
            Log.d("[FreeboxDiscoveryService]", "Freebox non trouvée");
            Toast.makeText(applicationContext, "Impossible de trouver la freebox", Toast.LENGTH_SHORT).show();
        }
    }
}
