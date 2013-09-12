/*
   Copyright 2011 Gerben CASTEL

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import fr.gcastel.freeboxV6GeekIncDownloader.R;
import fr.gcastel.freeboxV6GeekIncDownloader.utils.FreeboxAuthorization;
import fr.gcastel.freeboxV6GeekIncDownloader.utils.FreeboxDiscovery;
import fr.gcastel.freeboxV6GeekIncDownloader.utils.FreeboxDownload;

/**
 * Le service de téléchargement via freebox
 * 
 * @author Gerben
 */
public class FreeboxDownloaderService extends AsyncTask<String, Void, Void> {
  private static final String TAG = "FreeboxDownloaderService";
  private String urlFreeboxAPI = null;
  private Activity zeActivity;
  private boolean echec = false;
  private Dialog dialog;
  private String alertDialogMessage = null;
  private boolean bypassTraitement = false;
  private final static String APP_TOKEN_KEY = "APP_TOKEN_KEY";

  // Le tracking ID utilisé pour le challenge d'authentification freebox
  private String trackId = null;

  // Le challenge en cours avec la freebox
  private String challenge = null;

  private enum DialogEnCours {
    NONE,
    PROGRESS,
    ALERT
  }
  
  private DialogEnCours dialogueEnCours = DialogEnCours.NONE;
  
  /**
   * Instanciation
   */
  public FreeboxDownloaderService(Activity context, ProgressDialog inDialog, String urlFreeboxAPI, String trackId, String challenge) {
    zeActivity = context;
    dialog = inDialog;
    this.urlFreeboxAPI = urlFreeboxAPI;
    this.trackId = trackId;
    this.challenge = challenge;
    dialogueEnCours = DialogEnCours.PROGRESS;
  }

    public String getUrlFreeboxAPI() {
        return urlFreeboxAPI;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getTrackId() {
        return trackId;
    }


  
  private void prepareAlertDialog(String message) {
    dialogueEnCours = DialogEnCours.ALERT;
    alertDialogMessage = message;
  }

  private void showAlertDialog() {
    AlertDialog.Builder alertbox = new AlertDialog.Builder(zeActivity);
    alertbox.setMessage(alertDialogMessage);
    alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface arg0, int arg1) {
         dialogueEnCours = DialogEnCours.NONE;
      }
    });
    alertbox.show();
  }
    @Override
    protected Void doInBackground(String... params) {
        try {
            if (!bypassTraitement) {
                if (urlFreeboxAPI == null) {
                    // Recherche de la freebox
                    urlFreeboxAPI = FreeboxDiscovery.findFreeboxAPIURL();
                    if (urlFreeboxAPI == null) {
                        echec = true;
                        return null;
                    }
                }

                // Requête d'autorisation en cours
                if (trackId != null) {
                    challenge = FreeboxAuthorization.askForChallengeAfterTracking(urlFreeboxAPI, trackId);

                    // On annule la requête de toutes façons (ok ou nok)
                    trackId = null;
                }

                // Recherche de l'app token
                String appToken = zeActivity.getPreferences(Context.MODE_PRIVATE).getString(APP_TOKEN_KEY, "");
                if (appToken == null || ("".equals(appToken))) {
                    // Si l'app token n'est pas dispo, on en demande un
                    String authResponse = FreeboxAuthorization.sendAuthorizationRequest(zeActivity, urlFreeboxAPI);
                    Log.d("[FreeboxDownloaderService]", "Auth String : " + authResponse);

                    JSONObject jsonAuth = new JSONObject(authResponse);
                    if (jsonAuth.getBoolean("success")) {
                        JSONObject authData = jsonAuth.getJSONObject("result");

                        SharedPreferences.Editor editor = zeActivity.getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putString(APP_TOKEN_KEY, authData.getString("app_token"));
                        editor.commit();

                        // On récupère l'id de tracking pour vérifier la validation par l'utilisateur
                        trackId = authData.getString("track_id");
                        prepareAlertDialog("Autorisez cette application sur l'écran LCD de la Freebox et relancez le téléchargement.");
                    }
                } else {
                    Log.d("[FreeboxDownloaderService]", "App token dispo : " + appToken);

                    if (challenge == null) {
                        challenge = FreeboxAuthorization.askForChallenge(urlFreeboxAPI);
                    }
                    Log.d("[FreeboxDownloaderService]", "Challenge dispo : " + challenge);
                }

                // Si on a tous les composants, on peut se loguer !
                if (alertDialogMessage == null) {
                    if ((appToken!= null) && (challenge != null)) {
                        Log.d("[FreeboxDownloaderService]", "On a tout pour se loguer");
                        String sessionToken = FreeboxAuthorization.openSession(zeActivity, urlFreeboxAPI,appToken,challenge);
                        Log.d("[FreeboxDownloaderService]", "Session token : " + sessionToken);

                        Log.d("[FreeboxDownloaderService]", "Lancement du download : " + params[0]);
                        FreeboxDownload.launchDownload(urlFreeboxAPI,sessionToken,params[0]);
                    }
                } else {
                    echec = true;
                }
            }
        } catch (Exception e) {
            Log.d("[FreeboxDownloaderService]", "Exception lors du traitement", e);
            echec = true;
        }
        return null;
    }


    @Override
  protected void onPreExecute() {
    super.onPreExecute();
//    if (!NetworkTools.isConnectedViaWifi(zeActivity)) {
//      Toast.makeText(zeActivity, "Vous devez être connecté en Wifi pour accéder à la freebox", Toast.LENGTH_SHORT).show();
//      bypassTraitement = true;
//    } else {
      if (dialog != null) {
        dialog.show();
      }
//    }
  }

  @Override
  protected void onPostExecute(Void result) {
    super.onPostExecute(result);
    dialog.hide();
    dialogueEnCours = DialogEnCours.NONE;

    if (alertDialogMessage != null) {  
      showAlertDialog();
    }
    
    if (!bypassTraitement) {
      if (echec) {
        Toast.makeText(zeActivity, zeActivity.getString(R.string.cantConnectToFreebox), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(zeActivity, zeActivity.getString(R.string.downloadLaunched), Toast.LENGTH_SHORT).show();
      }
    }
  }
}
