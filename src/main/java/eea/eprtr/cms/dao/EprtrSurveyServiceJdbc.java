package eea.eprtr.cms.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import eea.eprtr.cms.model.EprtrSurvey;
import eea.eprtr.cms.model.EprtrSurveyItem;

public class EprtrSurveyServiceJdbc implements EprtrSurveyService{
    
	@SuppressWarnings("unused")
	private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void save(EprtrSurvey surv) {
        String query = "UPDATE SurveyMaster SET SurveyText=:surveyText, SurveyLabel=:surveyLabel, " 
        	+ "ListIndex=:listIndex"
            + " WHERE SurveyID=:surveyID";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(surv);
        namedParameterJdbcTemplate.update(query, namedParameters);

        for (EprtrSurveyItem surveyItem: surv.getEprtrSurveyItems()){
        	saveItem(surveyItem);
        }
    }

    @Override
    public void saveItem(EprtrSurveyItem survItem) {
        
    	String query = "UPDATE SurveyItems SET FK_SurveyID=:fkSurveyID, "
    			+ "SurveyItem=:surveyItem, SurveyItemResultID=:surveyItemResultID, "
    			+ "ListIndex=:listIndex"
    			+ " WHERE SurveyItemID=:surveyItemID";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(survItem);
        namedParameterJdbcTemplate.update(query, namedParameters);
    }

    @Override
    public EprtrSurveyItem newItem(Integer surveyID, Integer itemIndex) {
    	EprtrSurveyItem survItem = new EprtrSurveyItem(0,surveyID,"","",itemIndex);
    	String query = "INSERT INTO SurveyItems (FK_SurveyID,SurveyItem,SurveyItemResultID,ListIndex) "
    			+"VALUES ( :fkSurveyID, :surveyItem, :surveyItemResultID, :listIndex)";

    	KeyHolder keyHolder = new GeneratedKeyHolder();
    	SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(survItem);
        
    	namedParameterJdbcTemplate.update(query, namedParameters,keyHolder, new String[]{"ID"});
        survItem.setSurveyItemID(keyHolder.getKey().intValue());
        return survItem;
    }
    
    @Override
    public void deleteItem(Integer surveyItemID){
    	String query = "DELETE FROM SurveyItems "
  			+ "WHERE SurveyItemID = ?";
    	jdbcTemplate.update(query, new Object[]{surveyItemID});
    }
    
    @Override
    public void add(Integer itemIndex) {
    	EprtrSurvey surv = new EprtrSurvey("","Title",itemIndex);
    	String query = "INSERT INTO SurveyMaster (SurveyText,SurveyLabel,ListIndex) "
    			+"VALUES ( :surveyText, :surveyLabel, :listIndex)";

    	KeyHolder keyHolder = new GeneratedKeyHolder();
    	SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(surv);
        
    	namedParameterJdbcTemplate.update(query, namedParameters,keyHolder, new String[]{"ID"});
        surv.setSurveyID(keyHolder.getKey().intValue());
    }

    @Override
    public void delete(Integer surveyID){
    	String query = "DELETE FROM SurveyMaster "
  			+ "WHERE SurveyID = ?";
    	jdbcTemplate.update(query, new Object[]{surveyID});
    }

    
    @Override
    public EprtrSurvey getBySurveyID(int surveyID) {
        String query = "SELECT SurveyID, SurveyText, SurveyLabel, ListIndex"
        		+ " FROM SurveyMaster"
                + " WHERE SurveyID = ?";

            //using RowMapper anonymous class, we can create a separate RowMapper for reuse
        EprtrSurvey surv = jdbcTemplate.queryForObject(query, new Object[]{surveyID}, new RowMapper<EprtrSurvey>(){

                @Override
                public EprtrSurvey mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                	EprtrSurvey surv = new EprtrSurvey();
                	surv.setSurveyID(rs.getInt("SurveyID"));
                	surv.setSurveyText(rs.getString("SurveyText"));
                	surv.setSurveyLabel(rs.getString("surveyLabel")); 
                	surv.setListIndex(rs.getInt("listIndex"));
   //             	surv.setEprtrSurveyItems(getItemsBySurveyID(surv.getSurveyID()));
                    return surv;
                }});
           	surv.setEprtrSurveyItems(getItemsBySurveyID(surv.getSurveyID()));
            return surv;
    }


    @Override
    public List<EprtrSurveyItem> getItemsBySurveyID(int surveyID) {
        String query = "SELECT SurveyItemID, FK_SurveyID, SurveyItem, "
        		+ "SurveyItemResultID, ListIndex " 
        		+ "FROM SurveyItems WHERE FK_SurveyID = ?";
         
        List<EprtrSurveyItem> survItemsList = new ArrayList<EprtrSurveyItem>();

        List<Map<String, Object>> survItems = jdbcTemplate.queryForList(query, new Object[]{surveyID});

        for(Map<String, Object> survItem : survItems){
        	EprtrSurveyItem survi = new EprtrSurveyItem(
        			(Integer)survItem.get("SurveyItemID"),
        			(Integer)survItem.get("FK_SurveyID"),
        			(String)survItem.get("SurveyItem"),
        			(String)survItem.get("SurveyItemResultID"),
        			(Integer)survItem.get("listIndex"));
        	survItemsList.add(survi);
        }
        return survItemsList;
    }

    
    @Override
    public List<EprtrSurvey> getAll() {
        String query = "SELECT SurveyID, SurveyText, SurveyLabel, ListIndex "
            + " FROM SurveyMaster"
            + " ORDER BY ListIndex";

        List<EprtrSurvey> survList = new ArrayList<EprtrSurvey>();

        List<Map<String, Object>> survItems = jdbcTemplate.queryForList(query);

        for(Map<String, Object> survItem : survItems){
        	EprtrSurvey surv = new EprtrSurvey();
        	surv.setSurveyID((Integer)survItem.get("SurveyID"));
        	surv.setSurveyText((String)survItem.get("SurveyText"));
        	surv.setSurveyLabel((String)survItem.get("SurveyLabel"));
        	surv.setListIndex((Integer)survItem.get("ListIndex"));
        	//surv.setEprtrSurveyItems(getItemsBySurveyID((Integer)survItem.get("SurveyID")));
        	survList.add(surv);
        }
        return survList;
    }

    
}