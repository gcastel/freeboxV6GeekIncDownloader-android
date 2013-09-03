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
import android.util.Base64;
import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

        return NetworkTools.executeHTTPRequest("[FreeboxAuthorization]", postReq);
    }

    /**
     * Vérifie que la demande d'autorisation "trackée" est validée et récupère le challenge
     * à utiliser.
     *
     * @param urlFreeboxAPI l'url de l'API freebox
     * @param trackId l'identifiant de tracking
     * @return le challenge trouvé ou null si le trackId est refusé
     */
    public static String askForChallengeAfterTracking(String urlFreeboxAPI, String trackId) {
        String challenge = null;
        Log.d("[FreeboxAuthorization]", "Vérification du statut de l'autorisation");
        HttpGet getReq = new HttpGet(urlFreeboxAPI + "/login/authorize/" + trackId);

        String trackResult = NetworkTools.executeHTTPRequest("[FreeboxAuthorization]", getReq);

        if (trackResult == null) {
            return null;
        }

        try {
            JSONObject jsonResponse = new JSONObject(trackResult);
            if (jsonResponse.getBoolean("success")) {
                JSONObject authStatus = jsonResponse.getJSONObject("result");
                if ("granted".equals(authStatus.getString("status"))) {
                    challenge = authStatus.getString("challenge");
                }
            }
        } catch (JSONException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors du parsing de la réponse", e);
            challenge = null;
        }

        return challenge;
    }

    /**
     * Demande un challenge à la freebox
     *
     * @param urlFreeboxAPI l'url de l'API freebox
     * @return le challenge trouvé ou null si le trackId est refusé
     */
    public static String askForChallenge(String urlFreeboxAPI) {
        String challenge = null;
        Log.d("[FreeboxAuthorization]", "Demande de challenge");
        HttpGet getReq = new HttpGet(urlFreeboxAPI + "/login/");

        String trackResult = NetworkTools.executeHTTPRequest("[FreeboxAuthorization]", getReq);

        if (trackResult == null) {
            return null;
        }

        try {
            JSONObject jsonResponse = new JSONObject(trackResult);
            if (jsonResponse.getBoolean("success")) {
                JSONObject authStatus = jsonResponse.getJSONObject("result");
                challenge = authStatus.getString("challenge");
            }
        } catch (JSONException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors du parsing de la réponse", e);
            challenge = null;
        }

        return challenge;
    }

    /**
     * Ouvre une session auprès de la freebox et retourne son id
     *
     * @param urlFreeboxAPI l'url de l'API freebox
     * @param appToken le token d'application
     * @param challenge le challenge à utiliser
     * @return l'id de session ou null si échec
     */
    public static String openSession(String urlFreeboxAPI, String appToken, String challenge) {
        Log.d("[FreeboxAuthorization]", "Ouverture de session");

        HttpPost postReq = new HttpPost(urlFreeboxAPI + "/login/session/");

        StringEntity se;
        try {
            se = new StringEntity("{\n" +
                    "   \"app_id\": \"fr.gcastel.freeboxV6GeekIncDownloader\",\n" +
                    "   \"password\": \"" + hmacsha1(challenge,appToken) + "\"\n" +
                    "}");
        } catch (UnsupportedEncodingException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors de la création de la requête", e);
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors de la création de la requête", e);
            return null;
        } catch (InvalidKeyException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors de la création de la requête", e);
            return null;
        }

        postReq.setEntity(se);

        String result = NetworkTools.executeHTTPRequest("[FreeboxAuthorization]", postReq);

        if (result == null) {
            return null;
        }

        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(result);
            if (jsonResponse.getBoolean("success")) {
                JSONObject sessionResult = jsonResponse.getJSONObject("result");
                Log.d("[FreeboxAuthorization]", "Réponse reçue pour l'ouverture de session : "  + result);
                return sessionResult.getString("session_token");
            } else {
                return null;
            }
        } catch (JSONException e) {
            Log.e("[FreeboxAuthorization]", "Exception lors du parsing de la réponse : "  + result, e);
            return null;
        }
    }

    private static String hmacsha1(String challenge, String appToken) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec key = new SecretKeySpec(appToken.getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);

        byte[] bytes = mac.doFinal(challenge.getBytes("UTF-8"));

        return new String(Base64.encodeToString(bytes, 0));
    }
}
