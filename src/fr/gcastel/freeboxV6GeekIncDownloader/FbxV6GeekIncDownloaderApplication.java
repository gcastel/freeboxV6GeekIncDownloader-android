package fr.gcastel.freeboxV6GeekIncDownloader;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes( formKey = )
public class FbxV6GeekIncDownloaderApplication extends Application {
  @Override
  public void onCreate() {
      // The following line triggers the initialization of ACRA
      ACRA.init(this);
      super.onCreate();
  }
}
