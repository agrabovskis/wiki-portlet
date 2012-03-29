package lv.rtu.itd.wiki.portlets;

import java.io.Serializable;

public class WikiObject implements Serializable {    
    private static final long serialVersionUID = -1125804691610836759L;
    private String wikiText;
    private String name;
    private String PortletId;
    private String lang;
    private String title;
    private int id;
    
    
    public WikiObject(){
        wikiText="";
    }
    public WikiObject(String name,String lang){
        wikiText="";
        setName(name);
        setLang(lang);
    }
    public String getWikiText() {
        return wikiText;
    }
    
    public void setWikiText(String wikiText) {
        this.wikiText = wikiText;
    }
        
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public String getPortletId() {
        return PortletId;
    }
    public void setPortletId(String portletId) {
        PortletId = portletId;
    }
    public String getLang() {
        return lang;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
}
