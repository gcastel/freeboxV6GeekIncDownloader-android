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

import java.util.ArrayList;
import java.util.List;

/**
 * Le parser du flux RSS (parser à l'ancienne) 
 * @author Gerben
 */
public class GeekIncRSSParserService {

  private final String rssContent;
  
  public GeekIncRSSParserService(String rssContent) {
    this.rssContent = rssContent;
  }
  
  /**
   * Parsing XML à l'ancienne :
   *   recherche des éléments de début et de fin
   * @return la liste des blocs <item> du flux
   */
  private List<String> getItemsList() {
    List<String> itemsList = new ArrayList<String>();
    int posEnCours = 0;
    int nextPos;
   
    if (rssContent != null) {
      while ( (nextPos = rssContent.indexOf("<item>", posEnCours)) != -1) {
        int endPos = rssContent.indexOf("</item>", nextPos);
        String itemEnCours = rssContent.substring(nextPos,endPos);
        itemsList.add(itemEnCours);
        posEnCours = endPos + "<item>".length();
      }
    }
    
    return itemsList;
  }
  
  public List<PodcastElement> getPodcastElements() {
    List<PodcastElement> result = new ArrayList<PodcastElement>();
    
    List<String> itemsList = getItemsList();
    for (String itemContent : itemsList) {
      int titlePos = itemContent.indexOf("<title>") + "<title>".length();
      int endTitlePos = itemContent.indexOf("</title>", titlePos);
      String titre = itemContent.substring(titlePos, endTitlePos);
      titre = titre.substring("Geek Inc HD ".length());
      
      int urlPos = itemContent.indexOf("<enclosure url=") + "<enclosure url=".length() + 1;
      int endUrlPos = itemContent.indexOf("\"", urlPos);
      String url = itemContent.substring(urlPos, endUrlPos);
      
      PodcastElement element = new PodcastElement(titre, url);
      result.add(element);
    }
    
    return result;
  }
}
