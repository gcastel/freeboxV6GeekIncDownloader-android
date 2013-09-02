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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Gestion de l'autorisation d'accès de la freebox
 *
 * @author Gerben Castel
 */
public class FreeboxAuthorization {

    private void FreeboxAuthorization() {
        throw new AssertionError();
    }

    /**
     * Effectue la demande d'autorisation
     *
     * La chaîne de réponse ressemble à
     * {
     *   "success": true,
     *   "result": {
     *       "app_token": "dyNYgfK0Ya6FWGqq83sBHa7TwzWo+pg4fDFUJHShcjVYzTfaRrZzm93p7OTAfH/0",
     *    "track_id": 42
     *   }
     * }
     *
     * /!\ L'app token doit être enregistré pour les utilisations futures
     *
     * @param context le contexte de l'appli (pour récupérer les versions)
     * @param freeboxUrl l'URL de l'API freebox trouvée
     * @return la chaîne JSON de réponse à la demande de connexion
     */
    public static String sendAuthorizationRequest(Context context, String freeboxUrl) {
        HttpPost postReq = new HttpPost(freeboxUrl + "/login/authorize/");

        String versionName;
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException nnfe) {
            versionName = "";
        }

        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        StringEntity se;
        try {
            se = new StringEntity("{\n" +
                    "   \"app_id\": \"fr.gcastel.freeboxV6GeekIncDownloader\",\n" +
                    "   \"app_name\": \"GeekInc Downloader\",\n" +
                    "   \"app_version\": \""+ versionName + "\",\n" +
                    "   \"device_name\": \"Android v." + Build.VERSION.RELEASE + " - " + deviceId  + "\"\n" +
                    "}");
        } catch (UnsupportedEncodingException e) {
           Log.e("[FreeboxAuthorization]", "Exception lors de la création de la requête", e);
           return null;
        }

        postReq.setEntity(se);

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
            HttpResponse response = httpclient.execute(postReq);

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
                Log.d("[FreeboxAuthorization]", "Erreur d'autorisation, status code : "  + response.getStatusLine().getStatusCode());
                result = null;
            }
        } catch (IOException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors de l'autorisation", e);
            return null;
        }

        return result;
    }
}
