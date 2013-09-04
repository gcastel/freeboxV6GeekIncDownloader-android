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
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Méthodes utilitaires dédiées à la connexion réseau
 *
 * @author Gerben Castel
 */
public class NetworkTools {

    private NetworkTools() {
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

    /**
     * Exécute la requête HTTP
     *
     * @param logTag le tag à utiliser pour les logs
     * @param httpUriRequest la requête HTTP à exécuter
     * @param sessionToken la session à utiliser si nécessaire
     * @return le contenu du résultat
     */
    public static String executeHTTPRequest(String logTag, HttpUriRequest httpUriRequest, String sessionToken) {
        // Session
        if (sessionToken != null) {
          httpUriRequest.setHeader("X-Fbx-App-Auth",sessionToken);
        }

        // Envoi de la requête
        HttpParams httpParameters = new BasicHttpParams();

        // Mise en place de timeouts
        HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
        HttpConnectionParams.setSoTimeout(httpParameters, 5000);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        HttpParams params = httpclient.getParams();
        HttpClientParams.setRedirecting(params, false);

        String result = "";
        try {
            HttpResponse response = httpclient.execute(httpUriRequest);

            // Traitement de la réponse
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
                Log.d(logTag, "Erreur d'autorisation, status code : " + response.getStatusLine().getStatusCode());
                InputStream content = response.getEntity().getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(content));
                String line = br.readLine();

                while (line != null) {
                    result += line;
                    line = br.readLine();
                }

                br.close();
                content.close();
                Log.d(logTag, "Contenu " + result);
                result = null;
            }
        } catch (IOException e) {
            Log.e(logTag, "Exception lors de l'autorisation", e);
            result = null;
        }
        return result;
    }
}