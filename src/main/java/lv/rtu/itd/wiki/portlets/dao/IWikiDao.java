package lv.rtu.itd.wiki.portlets.dao;

import java.util.List;

import lv.rtu.itd.wiki.portlets.LangObject;
import lv.rtu.itd.wiki.portlets.WikiObject;

public interface IWikiDao {
    
    public WikiObject getWikiObjectById(int id);
    
    void addWikiObject(WikiObject wo);
    
    void updateWikiObject(WikiObject wo);
    
    public WikiObject getWikiObjectByName(String name, String portletId, String lang);
    
    public List<WikiObject> getWikiObjectsByPortletId(String portletID, String lang);

    //public List<LangObject> getAvialability(WikiObject wo);

    public WikiObject getWikiObjectByNameForEdit(String name, String portletID, String lang);

    public List<LangObject> getAvialability(String portletId, String pageName);

    
}
