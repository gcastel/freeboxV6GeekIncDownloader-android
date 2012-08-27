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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GeekIncLogoDownloadService {
	private URL toDownload;
	private File outFile;

	public GeekIncLogoDownloadService(String url, File cacheDir, String logoFile) {
		try {
			toDownload = new URL(url);
		} catch (MalformedURLException mue) {
			toDownload = null;
		}

		outFile = new File(cacheDir, logoFile);
	}

	private void saveAndResizeFile(File tmpFile, File outFile, int requiredSize) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(tmpFile);
		BufferedInputStream bfis = new BufferedInputStream(fis);

		// Recherche de la taille sans charger entièrement l'image
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(bfis, null, o);

		// Calcule la bonne mise à l'échelle en cherchant la puissance de 2 la
		// plus proche
		int scale = 1;
		while (o.outWidth / scale / 2 >= requiredSize
				&& o.outHeight / scale / 2 >= requiredSize)
			scale *= 2;

		// Mise à l'échelle !
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		
		bfis.close();
		fis.close();

		fis = new FileInputStream(tmpFile);
		bfis = new BufferedInputStream(fis);
		Bitmap bmp = BitmapFactory.decodeStream(bfis, null, o2);
		bfis.close();

		FileOutputStream fout = new FileOutputStream(outFile);
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		bmp.compress(Bitmap.CompressFormat.PNG, 90, bout);
		bout.close();
		fout.close();
	}

	public void downloadAndResize(int requiredSize) throws Exception {
		// Si le logo existe depuis moins d'une semaine, on ne le retélécharge
		// pas
		if (outFile.exists()) {
			// Moins une semaine
			long uneSemainePlusTot = System.currentTimeMillis()
					- (7 * 24 * 60 * 60 * 1000);

			if (outFile.lastModified() > uneSemainePlusTot) {
				Log.i("GeekIncLogoDownloadService",
						"Le logo a déjà été téléchargé il y a moins d'une semaine.");
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

		bis.close();
		is.close();
		
		// Fichier temporaire pour redimensionnement
		File tmpFile = new File(outFile.getAbsolutePath() + "tmp");
		FileOutputStream fos = new FileOutputStream(tmpFile);
		fos.write(baf.toByteArray());
		fos.close();

		saveAndResizeFile(tmpFile, outFile, requiredSize);

		// Suppression du fichier temporaire
		tmpFile.delete();
	}
}
