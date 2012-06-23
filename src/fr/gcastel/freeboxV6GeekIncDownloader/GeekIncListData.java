package fr.gcastel.freeboxV6GeekIncDownloader;

import java.util.List;

import fr.gcastel.freeboxV6GeekIncDownloader.datas.PodcastElement;

import android.app.ProgressDialog;

public class GeekIncListData {
  private ProgressDialog dialog = null; 
  private ProgressTask task = null;
  private String fluxRSS = null;
  private List<PodcastElement> podcastElements = null;
	public ProgressDialog getDialog() {
		return dialog;
	}
	public void setDialog(ProgressDialog dialog) {
		this.dialog = dialog;
	}
	public ProgressTask getTask() {
		return task;
	}
	public void setTask(ProgressTask task) {
		this.task = task;
	}
	public String getFluxRSS() {
		return fluxRSS;
	}
	public void setFluxRSS(String fluxRSS) {
		this.fluxRSS = fluxRSS;
	}
	public List<PodcastElement> getPodcastElements() {
		return podcastElements;
	}
	public void setPodcastElements(List<PodcastElement> podcastElements) {
		this.podcastElements = podcastElements;
	}
  
  
}
