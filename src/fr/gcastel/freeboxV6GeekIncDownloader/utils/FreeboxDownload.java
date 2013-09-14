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

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Le service de téléchargement sur la freebox
 *
 * @author Gerben Castel
 */
public class FreeboxDownload {
    private FreeboxDownload() {
        throw new AssertionError();
    }

    public static boolean launchDownload(String urlFreeboxAPI, String sessionToken, String downloadURL) {
        // Préparation des paramètres
        HttpPost postReq = new HttpPost(urlFreeboxAPI + "/downloads/add");

        List<NameValuePair> parametres = new ArrayList<NameValuePair>();
        parametres.add(new BasicNameValuePair("download_url", downloadURL));

        try {
            postReq.setEntity(new UrlEncodedFormEntity(parametres));
        } catch (UnsupportedEncodingException e) {
            Log.e("[FreeboxDownload]", "Erreur de lancement du téléchargement : ", e);
            return false;
        }

        String answer = NetworkTools.executeHTTPRequest("[FreeboxDownload]", postReq, sessionToken);
        if (answer == null) {
            return false;
        }
        try {
            JSONObject jsonAnswer = new JSONObject(answer);
            return jsonAnswer.getBoolean("success");
        } catch (JSONException e) {
            Log.e("[FreeboxDownload]", "Erreur de parsing de la réponse : ", e);
            return false;
        }
    }
}
