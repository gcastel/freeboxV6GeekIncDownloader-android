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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import fr.gcastel.freeboxV6GeekIncDownloader.R;
import fr.gcastel.freeboxV6GeekIncDownloader.utils.FreeboxAuthorization;
import fr.gcastel.freeboxV6GeekIncDownloader.utils.FreeboxDiscovery;

/**
 * Le service de téléchargement via freebox
 * 
 * @author Gerben
 */
public class FreeboxDownloaderService extends AsyncTask<String, Void, Void> {
  private static final String TAG = "FreeboxDownloaderService";
  private String urlFreebox;
  private Activity zeActivity;
  private boolean echec = false;
  private Dialog dialog;
  private String alertDialogMessage = null;
  private boolean bypassTraitement = false;
  private final static String APP_TOKEN_KEY = "APP_TOKEN_KEY";

  private enum DialogEnCours {
    NONE,
    PROGRESS,
    ALERT
  }
  
  private DialogEnCours dialogueEnCours = DialogEnCours.NONE;
  
  /**
   * Instanciation
   */
  public FreeboxDownloaderService(Activity context, ProgressDialog inDialog) {
    zeActivity = context;
    dialog = inDialog;
    dialogueEnCours = DialogEnCours.PROGRESS;
  }

  private String loginFreebox(String password) throws UnsupportedEncodingException, ClientProtocolException, IOException {
    String cookieFbx = "";
    String csrfToken = "";
    
    // Préparation des paramètres
    HttpPost postReq = new HttpPost(urlFreebox + "/login.php");
    List<NameValuePair> parametres = new ArrayList<NameValuePair>();
    parametres.add(new BasicNameValuePair("login", "freebox"));
    parametres.add(new BasicNameValuePair("passwd", password));
    postReq.setEntity(new UrlEncodedFormEntity(parametres));
    
    // Envoi de la requête
    HttpParams httpParameters = new BasicHttpParams();
    
    // Mise en place de timeouts
    int timeoutConnection = 5000;
    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    int timeoutSocket = 5000;
    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    
    HttpClient httpclient = new DefaultHttpClient(httpParameters);
    HttpParams params = httpclient.getParams();
    HttpClientParams.setRedirecting(params, false); 

    HttpResponse response= httpclient.execute(postReq);

    // Ok ? (302 = moved = redirection)
    if (response.getStatusLine().getStatusCode() == 302) {
      Header cookie = response.getFirstHeader("Set-Cookie");
      cookieFbx = cookie.getValue();
      
      // Extraction du cookie FBXSID
      cookieFbx = cookieFbx.substring(cookieFbx.indexOf("FBXSID=\""), cookieFbx.indexOf("\";")+1);
      Log.d(TAG, "Cookie = " + cookieFbx);
    } else {
      Log.d(TAG, "Erreur d'authentification - statusCode = " + response.getStatusLine().getStatusCode()  + " - reason = " + response.getStatusLine().getReasonPhrase());
      prepareAlertDialog("Erreur d'authentification");
    }
    
    // On a le cookie, il nous manque le csrf_token
    // On récupère la page download !
    HttpGet downloadPageReq = new HttpGet(urlFreebox + "/download.php");
    downloadPageReq.setHeader("Cookie", "FBXSID=\"" + cookieFbx + "\";");
    response = httpclient.execute(downloadPageReq);

    // Ok ?
    if (response.getStatusLine().getStatusCode() == 200) {
      HttpEntity entity = response.getEntity();
      InputStream contentStream = entity.getContent();
      
      BufferedReader br = new BufferedReader(new InputStreamReader(contentStream));
      String line = br.readLine();
      
      while (line != null) {
        if (line.contains("input type=\"hidden\" name=\"csrf_token\"")) {
        	csrfToken = line.substring(line.indexOf("value=\"") + "value=\"".length(), line.lastIndexOf("\""));
        	break;
        }
    	line = br.readLine();
      }
      
      br.close();
      contentStream.close();

      Log.d(TAG, "csrfToken = " + csrfToken);
    } else {
      Log.d(TAG, "Erreur d'authentification - statusCode = " + response.getStatusLine().getStatusCode()  + " - reason = " + response.getStatusLine().getReasonPhrase());
      prepareAlertDialog("Erreur d'authentification");
    }

    // C'est moche, mais ça me permet de corriger ça vite fait...
    return cookieFbx+ "<-->" + csrfToken;
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

  
  private void launchDownload(String cookieCSRF, String url) throws UnsupportedEncodingException, ClientProtocolException, IOException {
    int splitPos = cookieCSRF.indexOf("<-->");
	String cookie = cookieCSRF.substring(0, splitPos);
	String csrfToken = cookieCSRF.substring(splitPos + 4);  
	// Préparation des paramètres
    HttpPost postReq = new HttpPost(urlFreebox + "/download.cgi");
    List<NameValuePair> parametres = new ArrayList<NameValuePair>();
    parametres.add(new BasicNameValuePair("url", url));
    parametres.add(new BasicNameValuePair("user", "freebox"));
    parametres.add(new BasicNameValuePair("method", "download.http_add"));
    parametres.add(new BasicNameValuePair("csrf_token", csrfToken));
    postReq.setEntity(new UrlEncodedFormEntity(parametres));
    
    // Mise en place des headers
    postReq.setHeader("Cookie", cookie + ";");
    postReq.setHeader("Referer", "http://mafreebox.freebox.fr/download.php");
    
    // Envoi de la requête
    HttpParams httpParameters = new BasicHttpParams();
    
    // Mise en place de timeouts
    int timeoutConnection = 5000;
    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    int timeoutSocket = 5000;
    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    HttpClient httpclient = new DefaultHttpClient(httpParameters);
    HttpParams params = httpclient.getParams();
    HttpClientParams.setRedirecting(params, false); 

    HttpResponse response= httpclient.execute(postReq);

    // Ok ? (302 = moved = redirection)
    if (response.getStatusLine().getStatusCode() != 302) {
      Log.d(TAG, "Erreur lors du lancement du téléchargement - statusCode = " + response.getStatusLine().getStatusCode()  + " - reason = " + response.getStatusLine().getReasonPhrase());
      prepareAlertDialog("Erreur lors du lancement du téléchargement.");
    }  	
  }


    @Override
    protected Void doInBackground(String... params) {
        try {
            if (!bypassTraitement) {
                // Recherche de la freebox
                urlFreebox = FreeboxDiscovery.findFreeboxAPIURL();

                if (urlFreebox == null) {
                    echec = true;
                    return null;
                }

                // Recherche de l'app token
                String appToken = zeActivity.getPreferences(Context.MODE_PRIVATE).getString(APP_TOKEN_KEY, "");
                if (appToken == null || ("".equals(appToken))) {
                    // Si l'app token n'est pas dispo
                    String authResponse = FreeboxAuthorization.sendAuthorizationRequest(zeActivity, urlFreebox);
                    Log.d("[FreeboxDownloaderService]", "Auth String : " + authResponse);
                } else {
                    Log.d("[FreeboxDownloaderService]", "App token dispo : " + appToken);
                }
                return null;

//                String cookie = loginFreebox(params[1]);
//                if (alertDialogMessage == null) {
//                    launchDownload(cookie, params[0]);
//                } else {
//                    echec = true;
//                }
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
//    if (!ConnectionTools.isConnectedViaWifi(zeActivity)) {
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
