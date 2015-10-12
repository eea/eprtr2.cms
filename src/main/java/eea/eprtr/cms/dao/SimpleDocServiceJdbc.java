package eea.eprtr.cms.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import eea.eprtr.cms.model.SimpleDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public class SimpleDocServiceJdbc implements SimpleDocService {

    private DataSource dataSource;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void save(SimpleDoc doc) {
        //doc.setChangedDate = 
        String query = "UPDATE ReviseResourceValue SET ResourceValue=:content WHERE CultureCode='en-GB' AND ResourceValueID=:resourceValueID";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(doc);
        namedParameterJdbcTemplate.update(query, namedParameters);
    }

    @Override
    public SimpleDoc getByResourceValueID(int name) {
        String query = "SELECT ResourceValueID, KeyTitle, ResourceValue, ContentsGroupID, AllowHTML"
            + " FROM ReviseResourceKey"
            + " JOIN ReviseResourceValue ON ReviseResourceKey.ResourceKeyID = ReviseResourceValue.ResourceKeyID"
            + " WHERE ResourceValueID = ?";

        //using RowMapper anonymous class, we can create a separate RowMapper for reuse
        SimpleDoc doc = jdbcTemplate.queryForObject(query, new Object[]{name}, new RowMapper<SimpleDoc>(){

            @Override
            public SimpleDoc mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                SimpleDoc doc = new SimpleDoc();
                doc.setResourceValueID(rs.getInt("ResourceValueID"));
                doc.setTitle(rs.getString("KeyTitle"));
                doc.setAllowHTML(rs.getBoolean("AllowHTML"));
                doc.setContentsGroupID(rs.getInt("ContentsGroupID"));
                doc.setContent(rs.getString("ResourceValue"));
                doc.setAllowHTML(rs.getBoolean("AllowHTML"));
                return doc;
            }});

        return doc;
    }

    @Override
    public List<SimpleDoc> getAll() {
        String query = "SELECT ResourceValueID, ResourceKey, KeyTitle, ContentsGroupID, LOV_ContentsGroup.Name AS GroupName, AllowHTML"
            + " FROM ReviseResourceKey"
            + " JOIN ReviseResourceValue ON ReviseResourceKey.ResourceKeyID = ReviseResourceValue.ResourceKeyID"
            + " JOIN LOV_ContentsGroup ON ContentsGroupID = LOV_ContentsGroupID"
            + " AND CultureCode='en-GB'"
            + " ORDER BY ContentsGroupID, KeyTitle";

        List<SimpleDoc> docList = new ArrayList<SimpleDoc>();

        List<Map<String, Object>> docRows = jdbcTemplate.queryForList(query);

        for(Map<String, Object> docRow : docRows){
            SimpleDoc doc = new SimpleDoc();
            doc.setResourceValueID((Integer)docRow.get("ResourceValueID"));
            doc.setResourceKey((String)docRow.get("ResourceKey"));
            doc.setTitle(String.valueOf(docRow.get("KeyTitle")));
            doc.setContentsGroupName(String.valueOf(docRow.get("GroupName")));
            doc.setAllowHTML((Boolean)docRow.get("AllowHTML"));
            docList.add(doc);
        }
        return docList;
    }

}
