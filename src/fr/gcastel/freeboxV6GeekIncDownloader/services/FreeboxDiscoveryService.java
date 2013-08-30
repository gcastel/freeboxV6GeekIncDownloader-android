/*
   Copyright 2013 Gerben CASTEL

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

import android.os.AsyncTask;

/**
 * Le service de découverte de la freebox
 * Pour l'instant, on se repose sur l'adresse "mafreebox.freebox.fr".
 * Quand la base installée aura évoluée, il faudra effectuer une découverte réseau
 * avec l'API Android 4.1 NsdManager.DiscoveryListener.
 *
 * @author Gerben Castel
 */
public class FreeboxDiscoveryService extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        return null;
    }
}
