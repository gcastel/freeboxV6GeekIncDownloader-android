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
package fr.gcastel.freeboxV6GeekIncDownloader.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.os.Environment;
import android.util.Log;

public class GeekIncLogoDownloadService {
  private URL toDownload;
  private File outFile;
  
  public GeekIncLogoDownloadService(String url, String path) {
    try {
      toDownload = new URL(url);
      outFile = new File(Environment.getExternalStorageDirectory(), path);
      // Cr�ation des r�pertoires si n�cessaire
      String filePath = outFile.getPath();
      File destDir = new File(filePath.substring(0,filePath.lastIndexOf('/')));
      if (!destDir.exists()) {
        destDir.mkdirs();
      }
    } catch(MalformedURLException mue) {
      toDownload = null;
    }
  }
  
  public void download() throws Exception {
    // Si le logo existe depuis moins d'une semaine, on ne le ret�l�charge pas
    if (outFile.exists()) {
      // Moins une semaine
      long uneSemainePlusTot = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
      
      if (outFile.lastModified() > uneSemainePlusTot) {
        Log.i("GeekIncLogoDownloadService", "Le logo a d�j� �t� t�l�charg� il y a moins d'une semaine.");
        return;
      }
    }

    URLConnection ucon = toDownload.openConnection();
    InputStream is = ucon.getInputStream();
    
    BufferedInputStream bis = new BufferedInputStream(is);
    ByteArrayBuffer baf = new ByteArrayBuffer(50);
    int current = 0;
    while ((current = bis.read()) != -1) {
      baf.append((byte) current);
    }
    FileOutputStream fos = new FileOutputStream(outFile);
    fos.write(baf.toByteArray());
    fos.close();
  }
}
