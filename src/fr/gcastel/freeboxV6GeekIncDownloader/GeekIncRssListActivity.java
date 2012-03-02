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
 * L'activit� principale de l'application
 * 
 * @author Gerben
 */
public class GeekIncRssListActivity extends ListActivity {
  protected ProgressDialog dialog = null; 
  protected ProgressTask task = null;
  private String fluxRSS = null;
  private List<PodcastElement> podcastElements = null;

  /**
   * L'initialisation de l'activit�
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Le logo est-il pr�sent
    File geekIncLogoFile = new File(getCacheDir(), getString(R.string.geekIncLogoFileName));
    if (geekIncLogoFile.exists()) {
      loadImageInView(geekIncLogoFile);
    }
    
    task = (ProgressTask) getLastNonConfigurationInstance();
    if (task == null) {
      // S'il s'agit du premier lancement, on lance tout le syst�me
      // sinon, l'utilisateur devra faire "refresh"
      if (savedInstanceState == null) {
        launchReload();
      } else {
        // Restoration des donn�es
        fluxRSS = savedInstanceState.getString("fluxRSS");
        
        // Parsing
        // (d�l�guer �a � un thread ?)
        GeekIncRSSParserService parser = new GeekIncRSSParserService(fluxRSS);
        podcastElements = parser.getPodcastElements();
        updateView();
      }
    } else {
      task.attach(this);
      int oldProgress = 0;
      
      // On g�re le cas d'un changement d'activit� / rotation d'�cran
      // avant la cr�ation du dialogue
      if (dialog != null) {
      	oldProgress = dialog.getProgress(); 
      }
      
      // Si le dialogue doit encore �tre affich�, on le recr�e
      if (oldProgress < 100) {
        // Nouveau dialogue li� � cette activit�
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
   * Cr�ation du menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  /**
   * Ex�cute une commande li�e au menu
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
          getString(R.string.app_url) + "\n\nIc�ne :\n\"Hornet Icon Set\"\n(CC BY-NC-ND 3.0)\nhttp://878952.deviantart.com/");
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
   * Chargement de l'image d�sign�e
   *   
   * @param inFile le fichier contenant l'image � charger
   */
  private void loadImageInView(File inFile) {
    ImageView img = (ImageView) findViewById(R.id.geekIncHDLogo);
    img.setAdjustViewBounds(true);
    img.setMaxWidth((int) (getResources().getDisplayMetrics().density * 100 + 0.5f));
    img.setImageBitmap(BitmapFactory.decodeFile(inFile.getPath()));
    img.setVisibility(View.VISIBLE);
  }

  /**
   * Mise � jour de l'indicateur de progression
   * 
   * @param qty la quantit�
   * @param elements les �l�ments remont�s
   * @param inFluxRSS le flux RSS remont�
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
   * Mise � jour de la vue avec les donn�es en cours
   */
  void updateView() {
    // Le logo est-il pr�sent
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
