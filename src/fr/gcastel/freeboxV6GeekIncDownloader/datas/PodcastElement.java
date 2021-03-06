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
package fr.gcastel.freeboxV6GeekIncDownloader.datas;

public class PodcastElement {

  private final String titre;
  private final String url;
  private final String date;
  
  public PodcastElement(String titre, String url, String date) {
    super();
    this.titre = titre;
    this.url = url;
    this.date = date;
  }

  public String getTitre() {
    return titre;
  }

  public String getUrl() {
    return url;
  }
  
  public String getDate() {
  	return date;
  }
  
}
