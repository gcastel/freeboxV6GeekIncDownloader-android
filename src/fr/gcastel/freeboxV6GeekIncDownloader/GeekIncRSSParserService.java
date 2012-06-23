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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.gcastel.freeboxV6GeekIncDownloader.datas.PodcastElement;

/**
 * Le parser du flux RSS (parser à l'ancienne) 
 * @author Gerben
 */
public class GeekIncRSSParserService {

  private final String rssContent;
  private final DateFormat dateParser = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
  private final DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
  
  
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
    	// Recherche du titre
      int titlePos = itemContent.indexOf("<title>") + "<title>".length();
      int endTitlePos = itemContent.indexOf("</title>", titlePos);
      String titre = itemContent.substring(titlePos, endTitlePos);
      titre = titre.substring("Geek Inc HD ".length());
      
      // Recherche de l'url de la vidéo
      int urlPos = itemContent.indexOf("<feedburner:origEnclosureLink>") + "<feedburner:origEnclosureLink>".length();
      int endUrlPos = itemContent.indexOf("</feedburner:origEnclosureLink>", urlPos);

      String urlOrig = itemContent.substring(urlPos, endUrlPos);
      String url;
      if (urlOrig.startsWith("http://www.podtrac.com/pts/")) {
      	url = urlOrig;
      } else {
        int mediaUrlPos = urlOrig.indexOf("amp;media_url=") + "amp;media_url=".length();
        url = urlOrig.substring(mediaUrlPos);
      }
      
      // Recherche de la date de publication 
      int datePos = itemContent.indexOf("<pubDate>") + "<pubDate>".length();
      int endDatePos = itemContent.indexOf("</pubDate>", datePos);
      String dateToParse = itemContent.substring(datePos,endDatePos);
      
      PodcastElement element = new PodcastElement(titre, url, formatDate(dateToParse));
      result.add(element);
    }
    
    return result;
  }
  
  /**
   * Permet de parser la date de l'élément de flux RSS
   * 
   * @param inDate la date au format : "Thu, 16 Feb 2012 23:01:07 +0100"
   * @return la chaîne à afficher dans le programme
   */
  private String formatDate(String inDate) {
  	try {
  	  Date datePub = dateParser.parse(inDate);
  	  return dateFormatter.format(datePub);
  	} catch (ParseException pe) {
  		// C'est pas dramatique, on le cache à l'utilisateur
  		return "";
  	}
  }
}

