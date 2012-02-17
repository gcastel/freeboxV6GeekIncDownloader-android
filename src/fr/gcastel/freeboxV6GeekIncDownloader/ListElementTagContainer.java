package fr.gcastel.freeboxV6GeekIncDownloader;

import android.widget.TextView;

public class ListElementTagContainer {

	private final TextView titleView;
	private final TextView dateView;

	public ListElementTagContainer(TextView titleView, TextView dateView) {
		super();
		this.titleView = titleView;
		this.dateView = dateView;
	}

	public TextView getTitleView() {
		return titleView;
	}

	public TextView getDateView() {
		return dateView;
	}
}
