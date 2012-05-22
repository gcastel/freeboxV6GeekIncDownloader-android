package fr.gcastel.freeboxV6GeekIncDownloader;

import java.io.File;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;


public class GeekIncListController {
	private final GeekIncListData listData  = new GeekIncListData(); 
	
	/**
	 * M�thode g�rant la cr�ation de l'activit�
	 * (ce qui peut correspondre � une vraie cr�ation ou � une r�instanciation)
	 * 
	 * @param activity l'activit� en question
	 * @param savedInstanceState l'�tat conserv�
	 */
	public void handleActivityCreation(GeekIncRssListActivity activity, Bundle savedInstanceState) {
		
    // R�attachement d'une t�che �ventuelle
    ProgressTask task = listData.getTask();
    if (task == null) {
      // S'il s'agit du premier lancement, on lance tout le syst�me
      // sinon, l'utilisateur devra faire "refresh"
      if (savedInstanceState == null) {
        launchReload(activity);
      } else {
      	onRestoreInstanceState(savedInstanceState);
        updateView(activity);
      }
    } else {
    	// R�attachement
      task.attach(activity);
      int oldProgress = 0;
      
      // On g�re le cas d'un changement d'activit� / rotation d'�cran
      // avant la cr�ation du dialogue
      ProgressDialog dialog = listData.getDialog(); 
      if (dialog != null) {
      	oldProgress = dialog.getProgress(); 
      }
      
      // Si le dialogue doit encore �tre affich�, on le recr�e
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
            
    ProgressTask task = new ProgressTask(activity);
    task.execute();
    listData.setTask(task);
  }  
  
  private void instantiateAndShowProgressDialog(GeekIncRssListActivity activity, int progress) {
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
   * Mise � jour de la vue avec les donn�es en cours
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
   * Mise � jour de l'indicateur de progression
   * 
   * @param qty la quantit�
   * @param elements les �l�ments remont�s
   * @param inFluxRSS le flux RSS remont�
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
   * D�tache les t�ches en cours
   */
  public void detachTask() {
    if (listData.getTask() != null) {
    	listData.getTask().detach();
    }
  }
  
  /**
   * Lance un reload du flux RSS si pas d�j� en cours
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
  
}
