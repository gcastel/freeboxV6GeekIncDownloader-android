package fr.gcastel.freeboxV6GeekIncDownloader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import fr.gcastel.freeboxV6GeekIncDownloader.services.GeekIncLogoDownloadService;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * La classe de notre tâche asynchrone 
 * de chargement du flux RSS
 *  
 * @author Gerben
 */
public class ProgressTask extends AsyncTask<Void, Void, Void> {
  private GeekIncRssListActivity activity = null;
  private ProgressDialog dialog = null;
  private int progress = 0;
  private String fluxRSS = null;
  private List<PodcastElement> podcastElements;
  private final String logoDataPath;
  private final String logoFileName;

  /**
   * Le constructeur principal
   * 
   * @param activity l'activité liée
   */
  ProgressTask(GeekIncRssListActivity activity) {
    super();
    dialog = activity.dialog;
    logoDataPath = activity.getString(R.string.dataPath);
    logoFileName = activity.getString(R.string.geekIncLogoFileName);
    attach(activity);
  }

  /**
   * Méthode effectuant la recherche du flux RSS
   * 
   * @throws Exception
   *           en cas de problèmes
   */
  private void fetchFluxRSS() throws Exception {
    // Récupération du flux RSS
    StringBuilder fluxRssStringBuilder = new StringBuilder();
    HttpClient client = new DefaultHttpClient();
    HttpGet request = new HttpGet(
        activity.getString(R.string.geekIncRSSFeedURL));

    HttpResponse response = client.execute(request);

    // Traitement de la réponse
    InputStream repStream = response.getEntity().getContent();
    BufferedReader repReader = new BufferedReader(new InputStreamReader(
        repStream));

    String line = null;
    while ((line = repReader.readLine()) != null) {
      fluxRssStringBuilder.append(line);
      fluxRssStringBuilder.append('\n');
    }
    repReader.close();
    repStream.close();

    fluxRSS = fluxRssStringBuilder.toString();

  }

  /**
   * Méthode retournant l'url du logo GeekInc On ne fait pas de parsing XML
   * complet, c'est trop couteux
   * 
   * @return l'url trouvée
   */
  private String getGeekIncLogoURL() {
    // On cherche <channel> puis <image> puis <url></url>
    int indexChannel = fluxRSS.indexOf("<channel>");
    int indexImage = fluxRSS.indexOf("<image>", indexChannel + 1);
    int indexURL = fluxRSS.indexOf("<url>", indexImage + 1);

    // Extraction jusqu'à </url>
    return fluxRSS.substring(indexURL + 5,
        fluxRSS.indexOf("</url>", indexURL));
  }

  /*
   * C'est ici qu'on fait le requêtage du flux RSS GeekInc
   */
  @Override
  protected Void doInBackground(Void... unused) {

    // Récupération du flux RSS
    try {
      fetchFluxRSS();
    } catch (Exception ex) {
      Log.w("ProgressTask", "HTTP request error");
      return (null);
    }
    progress += 40;
    publishProgress();

    // Parsing
    String logoURL = getGeekIncLogoURL();
    progress += 10;
    publishProgress();

    // Récupération du logo GeekInc
    Log.i("ProgressTask", "URL trouvée : " + logoURL);
    GeekIncLogoDownloadService downService = new GeekIncLogoDownloadService(
        logoURL, logoDataPath + logoFileName);
    try {
      downService.download();
    } catch (Exception ex) {
      Log.w("ProgressTask", "HTTP download error " + ex.getMessage());
      return (null);
    }
    progress += 30;
    publishProgress();

    // Parsing du flux RSS pour générer les liens

    progress += 20;
    GeekIncRSSParserService parser = new GeekIncRSSParserService(fluxRSS);
    podcastElements = parser.getPodcastElements();
    publishProgress();

    return (null);
  }

  @Override
  protected void onProgressUpdate(Void... unused) {
    if (activity != null) {
      activity.updateProgress(getProgress(), podcastElements, fluxRSS);
    } else {
      Log.w("ProgressTask", "onProgressUpdate skipped, no activity");
    }
  }

  @Override
  protected void onPostExecute(Void unused) {
    if (activity != null) {
      if (fluxRSS != null) {
        Toast.makeText(activity, "Données à jour !", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(activity,
            "Impossible de récupérer le flux RSS de GeekInc HD !",
            Toast.LENGTH_SHORT).show();
        if (activity.dialog != null) {
          activity.dialog.hide();
        }
      }
    } else {
      Log.w("ProgressTask", "onPostExecute skipped, no activity");
    }
  }

  void detach() {
    dialog = activity.dialog;
    activity = null;
  }

  protected void attach(GeekIncRssListActivity inActivity) {
    activity = inActivity;
    activity.dialog = dialog;
  }

  public int getProgress() {
    return progress;
  }

  public List<PodcastElement> getPodcastElements() {
    return podcastElements;
  }
  

  public String getFluxRSS() {
    return fluxRSS;
  }
}