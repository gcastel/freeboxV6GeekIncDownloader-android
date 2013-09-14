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

import java.util.List;

import fr.gcastel.freeboxV6GeekIncDownloader.datas.PodcastElement;
import fr.gcastel.freeboxV6GeekIncDownloader.services.FreeboxDownloaderService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.os.AsyncTask;

public class ListPodcastAdapter extends BaseAdapter {

	private final ProgressDialog dialog;
	private final List<PodcastElement> elements;
	private final LayoutInflater layoutInflater;
	private FreeboxDownloaderService fbxService;
	private final Activity activity;
	private final String SAVED_PASSWORD_PREFERENCES_KEY;

	public ListPodcastAdapter(Activity inActivity, List<PodcastElement> elements) {
		super();
		this.elements = elements;
		activity = inActivity;
		layoutInflater = LayoutInflater.from(activity);
		dialog = new ProgressDialog(activity);
		dialog.setCancelable(true);
		dialog.setMessage(inActivity.getString(R.string.connectingToFreebox));
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		SAVED_PASSWORD_PREFERENCES_KEY = activity.getString(R.string.savedPasswordPreferencesKey);
		fbxService = new FreeboxDownloaderService(activity, dialog, null, null, null);
	}

	@Override
	public int getCount() {
		if (elements == null) {
			return 0;
		}
		return elements.size();
	}

	@Override
	public Object getItem(int position) {
		return elements.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	private void askForConfirmation(final String title, final String url) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setTitle("Lancement du téléchargement ?");
        dialogBuilder.setMessage("Lancer le téléchargement du " + title +" ?");

		// Lien des événements
		dialogBuilder.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (fbxService.getStatus() == AsyncTask.Status.PENDING) {
                                                  fbxService.execute(url);
						}
					}
				});
		dialogBuilder.setNegativeButton("Annuler", null);
	    dialogBuilder.show();		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		TextView title, date;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_item, parent,
					false);
			convertView.setTag(new ListElementTagContainer(
					         (TextView)convertView.findViewById(R.id.titleItem),
					         (TextView)convertView.findViewById(R.id.dateItem)));
		}
		
		title = ((ListElementTagContainer)convertView.getTag()).getTitleView();
		date = ((ListElementTagContainer)convertView.getTag()).getDateView();

		title.setText(elements.get(position).getTitre());
		date.setText(elements.get(position).getDate());
		
		
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Une tâche ne peut être exécutée qu'une fois
				if (fbxService.getStatus() == AsyncTask.Status.FINISHED) {
					fbxService = new FreeboxDownloaderService(activity, dialog, fbxService.getUrlFreeboxAPI(), fbxService.getTrackId(), fbxService.getChallenge());
				}
				if (fbxService.getStatus() == AsyncTask.Status.PENDING) {
//					if (NetworkTools.isConnectedViaWifi(activity)) {
					  askForConfirmation(elements.get(position).getTitre(), elements.get(position).getUrl());
//					} else {
//			           Toast.makeText(activity, activity.getString(R.string.NeedWifiToDownload), Toast.LENGTH_SHORT).show();
//					}
                }
			}
		});
		
		return convertView;
	}
	

	@Override
	public boolean isEmpty() {
		if (elements == null) {
			return true;
		}
		return elements.isEmpty();
	}

}
