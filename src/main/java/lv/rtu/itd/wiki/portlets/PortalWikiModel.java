package lv.rtu.itd.wiki.portlets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import lv.rtu.itd.wiki.portlets.dao.IWikiDao;

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.wiki.WikiModel;

public class PortalWikiModel implements WikiModel {
    
    private String url;
    
    private static final String AM = "&";
    private static final String PP = "pP_";
    public static final String PAGE_PARAM = "wiki_page";
    public static final String ACTION_PARAM = "action";
    public static final String LANG_PARAM = "lang";
    public static final String EDIT_ACTION = "edit";
    public static final String VIEW_ACTION = "view";
    public static final String LIST_ACTION = "list";
    public static final String ROOT_PAGE = "ROOT";
    private IWikiDao wikiDao;
    private String portletId;
    private String lang;
    
    public PortalWikiModel(String url, IWikiDao wikidao, String portletId, String lang) {
        this.wikiDao = wikidao;
        this.url = url;
        this.portletId = portletId;
        this.lang = lang;
    }
    
    @Override
    public boolean isDocumentAvailable(ResourceReference documentReference) {
        WikiObject wo = wikiDao.getWikiObjectByName(documentReference.getReference(), portletId, lang);
        if (wo == null) {
            return false;
        }
        return true;
    }
    
    @Override
    public String getLinkURL(ResourceReference arg0) {
        // TODO Auto-generated method stub
        return "LINK";
    }
    
    @Override
    public String getImageURL(ResourceReference arg0, Map<String, String> arg1) {
        // TODO Auto-generated method stub
        return "IMAGE";
    }
    
    @Override
    public String getDocumentViewURL(ResourceReference documentReference) {
        String wikiPage = documentReference.getReference();
        try {
            wikiPage = URLEncoder.encode(wikiPage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return url + (wikiPage != null ? AM + PP + PAGE_PARAM + "=" + wikiPage : "");
    }
    
    @Override
    public String getDocumentEditURL(ResourceReference documentReference) {
        String tmpUrl = "";
        tmpUrl += url;
        String wikiPage = documentReference.getReference();
        try {
            wikiPage = URLEncoder.encode(wikiPage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        tmpUrl += (wikiPage != null ? AM + PP + PAGE_PARAM + "=" + wikiPage : "");
        tmpUrl += (wikiPage != null ? AM + PP + ACTION_PARAM + "=" + EDIT_ACTION : "");
        tmpUrl += (wikiPage != null ? AM + PP + LANG_PARAM + "=" + lang : "");
        
        return tmpUrl;
    }
    
}
