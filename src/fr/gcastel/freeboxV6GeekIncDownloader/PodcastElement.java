package fr.gcastel.freeboxV6GeekIncDownloader;

public class PodcastElement {

  private final String titre;
  private final String url;
  
  public PodcastElement(String titre, String url) {
    super();
    this.titre = titre;
    this.url = url;
  }

  public String getTitre() {
    return titre;
  }

  public String getUrl() {
    return url;
  }
  
  
}
