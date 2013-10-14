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

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.BASE)
@ReportsCrashes(formKey = "",
        formUri="http://applicationId.appspot.com/acrareport",
        formUriBasicAuthLogin = "user",
        formUriBasicAuthPassword = "password",
        mode = ReportingInteractionMode.SILENT,
        resNotifTickerText = R.string.crash_notif_ticker_text,
        resNotifTitle = R.string.crash_notif_title,
        resNotifText = R.string.crash_notif_text,
        resNotifIcon = android.R.drawable.stat_notify_error,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        resDialogOkToast = R.string.crash_dialog_ok_toast)
public class FbxV6GeekIncDownloaderApplication extends Application {
  @Override
  public void onCreate() {
      // The following line triggers the initialization of ACRA
      //ACRA.init(this);
      super.onCreate();
  }
}
