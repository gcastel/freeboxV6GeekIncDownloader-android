/*
   Copyright 2012 Gerben CASTEL

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
package fr.gcastel.freeboxV6GeekIncDownloader.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Tâche asynchrone effectuant la mise à l'échelle d'une image
 * 
 * @author Gerben Castel
 *
 */
public class BitmapResizerTask extends AsyncTask<File, Integer, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final int desiredWidth;

    public BitmapResizerTask(ImageView imageView, int width) {
        // On utilise une weakreference pour ne pas empêcher la garbage collection
        imageViewReference = new WeakReference<ImageView>(imageView);
        desiredWidth = width;
    }

    // Décodage en arrière plan
    @Override
    protected Bitmap doInBackground(File... params) {
        File file = params[0];
        return decodeFile(file, desiredWidth);
    }

    // Si l'imageview existe encore à la fin du traitement, on affiche
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    
    // Décode l'image et la met à l'échelle en recherchant la puissance de 2 la plus proche
    private Bitmap decodeFile(File f, int requiredSize){
      try {
          // Recherche de la taille sans charger entièrement l'image
          BitmapFactory.Options o = new BitmapFactory.Options();
          o.inJustDecodeBounds = true;
          BitmapFactory.decodeStream(new FileInputStream(f),null,o);

          // Calcule la bonne mise à l'échelle en cherchant la puissance de 2 la plus proche
          int scale=1;
          while(o.outWidth/scale/2>=requiredSize && o.outHeight/scale/2>=requiredSize)
              scale*=2;

          // Mise à l'échelle !
          BitmapFactory.Options o2 = new BitmapFactory.Options();
          o2.inSampleSize=scale;
          return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
      } catch (FileNotFoundException e) {}
      return null;
    }
}
