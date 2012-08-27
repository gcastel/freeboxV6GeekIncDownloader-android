package fr.gcastel.freeboxV6GeekIncDownloader;

import java.io.File;
import java.util.List;

import fr.gcastel.freeboxV6GeekIncDownloader.datas.GeekIncListData;
import fr.gcastel.freeboxV6GeekIncDownloader.datas.PodcastElement;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


public class GeekIncListController {
	private final GeekIncListData listData  = new GeekIncListData(); 
	
	/**
	 * Méthode gérant la création de l'activité
	 * (ce qui peut correspondre à une vraie création ou à une réinstanciation)
	 * 
	 * @param activity l'activité en question
	 * @param savedInstanceState l'état conservé
	 */
	public void handleActivityCreation(GeekIncRssListActivity activity, Bundle savedInstanceState) {
		
    // Réattachement d'une tâche éventuelle
    ProgressTask task = listData.getTask();
    if (task == null) {
      // S'il s'agit du premier lancement, on lance tout le système
      // sinon, l'utilisateur devra faire "refresh"
      if (savedInstanceState == null) {
        launchReload(activity);
      } else {
      	onRestoreInstanceState(savedInstanceState);
        updateView(activity);
      }
    } else {
    	// Réattachement
      task.attach(activity);
      int oldProgress = 0;
      
      // On gère le cas d'un changement d'activité / rotation d'écran
      // avant la création du dialogue
      ProgressDialog dialog = listData.getDialog(); 
      if (dialog != null) {
      	oldProgress = dialog.getProgress(); 
      }
      
      // Si le dialogue doit encore être affiché, on le recrée
      if (oldProgress < 100) {
      	instantiateAndShowProgressDialog(activity, oldProgress);
      }
      updateProgress(activity, task.getProgress(), task.getPodcastElements(), task.getFluxRSS());
    }
	}
	
  
  /**
   * Lancement d'un rechargement du flux
   */
  private void launchReload(GeekIncRssListActivity activity) {
  	instantiateAndShowProgressDialog(activity, 0);
            
    ImageView img = (ImageView) activity.findViewById(R.id.geekIncHDLogo);
    img.setAdjustViewBounds(true);
    int width = (int) (activity.getResources().getDisplayMetrics().density * 100 + 0.5f);
    img.setMaxWidth(width);
  	
    ProgressTask task = new ProgressTask(activity, width);
    task.execute();
    listData.setTask(task);
  }  
  
  private void instantiateAndShowProgressDialog(GeekIncRssListActivity activity, int progress) {
  	if (listData.getDialog() != null) {
  		listData.getDialog().dismiss();
  	}
	  
	ProgressDialog dialog = new ProgressDialog(activity);
    dialog.setCancelable(true);
    dialog.setMessage("Chargement...");
    // set the progress to be horizontal
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    // reset the bar to the default value of 0
    dialog.setProgress(progress);
    dialog.setMax(100);
    dialog.show();
    listData.setDialog(dialog);
  }
  
  
  /**
   * Mise à jour de la vue avec les données en cours
   */
  private void updateView(GeekIncRssListActivity activity) {
    // Le logo est-il pr�sent
    File geekIncLogoFile = new File(
        activity.getCacheDir(),
        activity.getString(R.string.geekIncLogoFileName));
    if (geekIncLogoFile.exists()) {
    	activity.loadImageInView(geekIncLogoFile);
    }

    // Mise en place de la liste
    activity.setListAdapter(new ListPodcastAdapter(activity, listData.getPodcastElements()));
  }
  

  /**
   * Mise à jour de l'indicateur de progression
   * 
   * @param qty la quantité
   * @param elements les éléments remontés
   * @param inFluxRSS le flux RSS remonté
   */
  public void updateProgress(GeekIncRssListActivity activity, int qty, List<PodcastElement> elements, String inFluxRSS) {
    listData.getDialog().setProgress(qty);
    if (inFluxRSS != null) {
    	listData.setFluxRSS(inFluxRSS);
    }
    
    if (elements != null) {
    	listData.setPodcastElements(elements);
    }

    if (qty >= 100) {
      // Fini !!
    	listData.getDialog().hide();
      updateView(activity);
    }
  }
  
  /**
   * Détache les tâches en cours
   */
  public void detachTask() {
    if (listData.getTask() != null) {
    	listData.getTask().detach();
    }
  }
  
  /**
   * Lance un reload du flux RSS si pas déjà en cours
   */
  public void launchReloadIfNeeded(GeekIncRssListActivity activity) {
    if ((listData.getTask() == null) || 
        ((listData.getDialog() != null) && (listData.getDialog().getProgress() >= 100))) {
      launchReload(activity);
    }
  }
  
  public ProgressDialog getDialog() {
  	return listData.getDialog();
  }
  
  public void setDialog(ProgressDialog dialog) {
  	listData.setDialog(dialog);
  }
  
  
  public void onRestoreInstanceState(Bundle savedInstanceState) {
  	listData.setFluxRSS(savedInstanceState.getString("fluxRSS"));
    GeekIncRSSParserService parser = new GeekIncRSSParserService(listData.getFluxRSS());
    try {
      listData.setPodcastElements(parser.getPodcastElements()); 
    } catch (Exception ex) {
      Log.w("GeekIncListController", "Parsing error " + ex.getMessage());
    }
  }
  
  public void onSaveInstanceState(Bundle outState) {
  	outState.putString("fluxRSS", listData.getFluxRSS());
  }
  
  public void dismissViews() {
	  if (listData.getDialog() != null) {
		  listData.getDialog().dismiss();
	  }
  }
}
