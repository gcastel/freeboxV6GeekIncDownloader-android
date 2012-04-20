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

import android.app.AlertDialog;
import android.app.ListActivity;
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

	protected GeekIncListController listController = null; 
	
  /**
   * L'initialisation de l'activité
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    // On réattache les données à la vue
    listController = (GeekIncListController)getLastNonConfigurationInstance();
    if (listController == null) {
    	listController = new GeekIncListController();
    }
    
    // Le logo est-il présent
    File geekIncLogoFile = new File(getCacheDir(), getString(R.string.geekIncLogoFileName));
    if (geekIncLogoFile.exists()) {
      loadImageInView(geekIncLogoFile);
    }
    
    // Initialisation ou mise à jour de l'activité
    listController.handleActivityCreation(this, savedInstanceState);
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
      listController.launchReload();
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

  /**
   * Retourne le contrôleur lors d'un switch d'activité
   * et détâche les tâches en cours
   */
  @Override
  public Object onRetainNonConfigurationInstance() {
  	listController.detachTask();
    return listController;
  }

  /**
   * Chargement de l'image désignée
   *   
   * @param inFile le fichier contenant l'image à charger
   */
  public void loadImageInView(File inFile) {
    ImageView img = (ImageView) findViewById(R.id.geekIncHDLogo);
    img.setAdjustViewBounds(true);
    img.setMaxWidth((int) (getResources().getDisplayMetrics().density * 100 + 0.5f));
    img.setImageBitmap(BitmapFactory.decodeFile(inFile.getPath()));
    img.setVisibility(View.VISIBLE);
  }
}
