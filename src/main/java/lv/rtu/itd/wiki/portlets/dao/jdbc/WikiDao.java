package lv.rtu.itd.wiki.portlets.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import lv.rtu.itd.wiki.portlets.LangObject;
import lv.rtu.itd.wiki.portlets.WikiObject;
import lv.rtu.itd.wiki.portlets.dao.IWikiDao;

public class WikiDao extends NamedParameterJdbcDaoSupport implements IWikiDao {
    
    @Override
    public WikiObject getWikiObjectById(int id) {
        
        String sql = "SELECT * FROM article WHERE id = :id;";
        Map<String, Integer> params = new HashMap<String, Integer>();
        params.put("id", id);
        WikiObject result = null;
        try {
            result = (WikiObject) getNamedParameterJdbcTemplate().queryForObject(sql, params, new WikiRowMapper());
        } catch (EmptyResultDataAccessException e) {
        }
        return result;
    }
    
    @Override
    public WikiObject getWikiObjectByNameForEdit(String name, String portletID, String lang) {
        
        String sql = "SELECT title, a.id, wikitext, \"name\", portlet_id, lang_medium " + //
                "FROM article a, lang l " + //
                "WHERE " + //
                "name = :name " + //
                "AND " + //
                "portlet_id = :portlet_id " + //
                "AND " + //
                "lang_medium = :lang " +
                "AND " + //
                "lang_id = l.id " +
                "";//
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("portlet_id", portletID);
        params.put("lang", lang);
        WikiObject result = null;
        try {
            result = (WikiObject) getNamedParameterJdbcTemplate().queryForObject(sql, params, new WikiRowMapper());
        } catch (EmptyResultDataAccessException e) {
        }
        return result;
    }
    
    
    @Override
    public WikiObject getWikiObjectByName(String name, String portletID, String lang) {
        String sqlStart = "SELECT tmpId, title, a.id, wikitext, \"name\", portlet_id, lang_medium " +//
        		"FROM article a, lang l,( values (%s) ) AS j(tmpId)" +
        		"WHERE " +//
        		"lang_id = l.id " +//
        		"AND " + //
        		"name = :name " + //
        		"AND " +//
        		"portlet_id = :portlet_id ";//
        String sql ="SELECT * from ("+ String.format(sqlStart, "0") + //
                "AND " + //
                "lang_medium = :lang " +
                "UNION " +//
                String.format(sqlStart, "1") +//
                " ) list_o Order by tmpId    LIMIT 1"; //
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("portlet_id", portletID);
        params.put("lang", lang);
        WikiObject result = null;
        try {
            result = (WikiObject) getNamedParameterJdbcTemplate().queryForObject(sql, params, new WikiRowMapper());
        } catch (EmptyResultDataAccessException e) {
        }
        return result;
    }
    
    public int getNextId() {
        String sql = "SELECT nextval('article_id_seq')";
        int result = getJdbcTemplate().queryForInt(sql);
        return result;
    }
    
    protected class WikiRowMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet resultSet, int rowIndex) throws SQLException {
            WikiObject object = new WikiObject();
            object.setId(resultSet.getInt("id"));
            object.setWikiText(resultSet.getString("wikitext"));
            object.setName(resultSet.getString("name"));
            object.setPortletId(resultSet.getString("portlet_id"));
            object.setLang(resultSet.getString("lang_medium"));
            object.setTitle(resultSet.getString("title"));
            return object;
        }
    }
    protected class LangRowMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet resultSet, int rowIndex) throws SQLException {
            LangObject object = new LangObject();
            object.setLang(resultSet.getString("lang_medium"));
            object.setExist(resultSet.getBoolean("exist"));
            return object;
        }
    }
    
    @Override
    public void addWikiObject(WikiObject wo) {
        wo.setId(getNextId());
        String sql = "INSERT INTO article " +//
        		"(id,  wikitext,  name,  portlet_id,title,  lang_id) " + //
        		"VALUES " +
        		"(:id, :wikiText, :name, :portlet_id,:title, (select id from lang where lang_medium = :lang));";
        Map<String, Object> named = new HashMap<String, Object>();
        named.put("wikiText", wo.getWikiText());
        named.put("name", wo.getName().trim());
        named.put("portlet_id", wo.getPortletId());
        named.put("id", wo.getId());
        named.put("lang", wo.getLang());
        named.put("title", wo.getTitle().trim());
        getNamedParameterJdbcTemplate().update(sql, named);
    }
    
    @Override
    public void updateWikiObject(WikiObject wo) {
        // wo.setId(getNextId());
        
        String sql = "UPDATE article " + //
                "SET " + //
                "wikitext=:wikiText , " + //
                "portlet_id=:portlet_id, " + //
                "title=:title " + //
                "WHERE id=:id; ";
        System.out.println("AAAAAAAAAAAAAAAA  ID: " + wo.getId());
        Map<String, Object> named = new HashMap<String, Object>();
        named.put("wikiText", wo.getWikiText());
        named.put("name", wo.getName().trim());
        named.put("portlet_id", wo.getPortletId());
        named.put("title", wo.getTitle().trim());
        named.put("id", wo.getId());
        getNamedParameterJdbcTemplate().update(sql, named);
    }
    
    @Override
    public List<WikiObject> getWikiObjectsByPortletId(String portletID, String lang) {
        
        String sql = "SELECT title, a.id, wikitext, \"name\", portlet_id, lang_medium " + //
                "FROM article a, lang l " + //
                "WHERE " + //
                "portlet_id = :portlet_id " + //
                "AND " + //
                "lang_medium = :lang " +
                "AND " + //
                "lang_id = l.id " +
                "";// 
        Map<String, String> params = new HashMap<String, String>();
        params.put("portlet_id", portletID);
        params.put("lang", lang);
                
        @SuppressWarnings("unchecked")
        List<WikiObject> result = getNamedParameterJdbcTemplate().query(sql, params, new WikiRowMapper());
        return result;
    }
  //  @Override
    public List<LangObject> getAvialability(WikiObject wo) {

        return getAvialability(wo.getPortletId(),wo.getName());
    }
    @Override
 public List<LangObject> getAvialability(String portletId, String pageName) {
        
        String sql = "" + //
                "select " + //
                " l.lang_medium, " + //
                " (a.lang_id is not null) exist " + //
                "from lang l " + //
                "left join article a " + //
                "on " + //
                " a.lang_id = l.id " + //
                "and " + //
                " name = :name " + //
                "and " + //
                " portlet_id = :portlet_id ";
        Map<String, String> params = new HashMap<String, String>();
        
        params.put("name", portletId);
        params.put("portlet_id", pageName);
        @SuppressWarnings("unchecked")
        List<LangObject> result =  getNamedParameterJdbcTemplate().query(sql, params, new LangRowMapper());
        return result;
    }
    
}
