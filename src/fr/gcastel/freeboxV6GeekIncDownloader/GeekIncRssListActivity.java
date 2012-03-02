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
package fr.gcastel.freeboxV6GeekIncDownloader;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * L'activité principale de l'application
 * 
 * @author Gerben
 */
public class GeekIncRssListActivity extends ListActivity {
  protected ProgressDialog dialog = null; 
  protected ProgressTask task = null;
  private String fluxRSS = null;
  private List<PodcastElement> podcastElements = null;

  /**
   * L'initialisation de l'activité
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Le logo est-il présent
    File geekIncLogoFile = new File(getCacheDir(), getString(R.string.geekIncLogoFileName));
    if (geekIncLogoFile.exists()) {
      loadImageInView(geekIncLogoFile);
    }
    
    task = (ProgressTask) getLastNonConfigurationInstance();
    if (task == null) {
      // S'il s'agit du premier lancement, on lance tout le système
      // sinon, l'utilisateur devra faire "refresh"
      if (savedInstanceState == null) {
        launchReload();
      } else {
        // Restoration des données
        fluxRSS = savedInstanceState.getString("fluxRSS");
        
        // Parsing
        // (déléguer ça à un thread ?)
        GeekIncRSSParserService parser = new GeekIncRSSParserService(fluxRSS);
        podcastElements = parser.getPodcastElements();
        updateView();
      }
    } else {
      task.attach(this);
      int oldProgress = 0;
      
      // On gère le cas d'un changement d'activité / rotation d'écran
      // avant la création du dialogue
      if (dialog != null) {
      	oldProgress = dialog.getProgress(); 
      }
      
      // Si le dialogue doit encore être affiché, on le recrée
      if (oldProgress < 100) {
        // Nouveau dialogue lié à cette activité
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("Chargement...");
        // set the progress to be horizontal
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // reset the bar to the default value of 0
        dialog.setProgress(oldProgress);
        dialog.setMax(100);
        dialog.show();        
      }
      updateProgress(task.getProgress(), task.getPodcastElements(), task.getFluxRSS());
    }
  }

  /**
   * Création du menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  /**
   * Exécute une commande liée au menu
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.refreshButton:
      if ((task == null) || 
          ((dialog != null) && (dialog.getProgress() >= 100))) {
        launchReload();
      }
      
      return true;
    case R.id.aboutButton:
      AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
      String versionName = null;
      try {
        PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        versionName = pinfo.versionName;
      } catch (NameNotFoundException nnfe) {
        versionName = "";
      }
      alertbox.setMessage(
          getString(R.string.app_name) + " v" + versionName + "\n\n" +
          "(c) Gerben Castel 2011\n" +
          getString(R.string.app_url) + "\n\nIcône :\n\"Hornet Icon Set\"\n(CC BY-NC-ND 3.0)\nhttp://878952.deviantart.com/");
      alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
           return;
        }
      });
      alertbox.show();
      return true;      
    default:
      return super.onOptionsItemSelected(item);
    }
  }
  
  private void launchReload() {
    dialog = new ProgressDialog(this);
    dialog.setCancelable(true);
    dialog.setMessage("Chargement...");
    // set the progress to be horizontal
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    // reset the bar to the default value of 0
    dialog.setProgress(0);
    dialog.setMax(100);
    dialog.show();
            
    task = new ProgressTask(this);
    task.execute();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    fluxRSS = savedInstanceState.getString("fluxRSS");
    super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString("fluxRSS", fluxRSS);
    super.onSaveInstanceState(outState);
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    if (task != null) {
      task.detach();
    }
    return (task);
  }

  @Override
  public void onStart() {
    super.onStart(); 
  }

  /**
   * Chargement de l'image désignée
   *   
   * @param inFile le fichier contenant l'image à charger
   */
  private void loadImageInView(File inFile) {
    ImageView img = (ImageView) findViewById(R.id.geekIncHDLogo);
    img.setAdjustViewBounds(true);
    img.setMaxWidth((int) (getResources().getDisplayMetrics().density * 100 + 0.5f));
    img.setImageBitmap(BitmapFactory.decodeFile(inFile.getPath()));
    img.setVisibility(View.VISIBLE);
  }

  /**
   * Mise à jour de l'indicateur de progression
   * 
   * @param qty la quantité
   * @param elements les éléments remontés
   * @param inFluxRSS le flux RSS remonté
   */
  void updateProgress(int qty, List<PodcastElement> elements, String inFluxRSS) {
    dialog.setProgress(qty);
    if (inFluxRSS != null) {
      fluxRSS = inFluxRSS;
    }
    
    if (elements != null) {
      podcastElements = elements;
    }

    if (qty >= 100) {
      // Fini !!
      dialog.hide();
      updateView();
    }
  }
  
  /**
   * Mise à jour de la vue avec les données en cours
   */
  void updateView() {
    // Le logo est-il présent
    File geekIncLogoFile = new File(
        getCacheDir(),
        getString(R.string.geekIncLogoFileName));
    if (geekIncLogoFile.exists()) {
      loadImageInView(geekIncLogoFile);
    }

    // Mise en place de la liste
    setListAdapter(new ListPodcastAdapter(this, podcastElements));
  }
}
