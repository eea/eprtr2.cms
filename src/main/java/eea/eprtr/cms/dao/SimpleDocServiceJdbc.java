package eea.eprtr.cms.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import eea.eprtr.cms.model.SimpleDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

public class SimpleDocServiceJdbc implements SimpleDocService {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public SimpleDoc getByResourceKey(String name) {
        String query = "SELECT ResourceKey, KeyTitle, ResourceValue, AllowHTML, ContentsGroupID"
            + " FROM ReviseResourceKey"
            + " JOIN ReviseResourceValue ON ReviseResourceKey.ResourceKeyID = ReviseResourceValue.ResourceKeyID"
            + " AND CultureCode='en-GB' WHERE ResourceKey = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        //using RowMapper anonymous class, we can create a separate RowMapper for reuse
        SimpleDoc doc = jdbcTemplate.queryForObject(query, new Object[]{name}, new RowMapper<SimpleDoc>(){

            @Override
            public SimpleDoc mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                SimpleDoc doc = new SimpleDoc();
                doc.setResourceKey(rs.getString("ResourceKey"));
                doc.setTitle(rs.getString("KeyTitle"));
                doc.setAllowHTML(rs.getBoolean("AllowHTML"));
                doc.setContentsGroupID(rs.getInt("ContentsGroupID"));
                doc.setContent(rs.getString("ResourceValue"));
                return doc;
            }});

        return doc;
    }

    @Override
    public List<SimpleDoc> getAll() {
        String query = "SELECT ResourceKey, KeyTitle, ContentsGroupID, LOV_ContentsGroup.Name AS GroupName"
            + " FROM ReviseResourceKey"
            + " JOIN ReviseResourceValue ON ReviseResourceKey.ResourceKeyID = ReviseResourceValue.ResourceKeyID"
            + " JOIN LOV_ContentsGroup ON ContentsGroupID = LOV_ContentsGroupID"
            + " AND CultureCode='en-GB'"
            + " ORDER BY GroupName, KeyTitle";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<SimpleDoc> docList = new ArrayList<SimpleDoc>();

        List<Map<String, Object>> docRows = jdbcTemplate.queryForList(query);

        for(Map<String, Object> docRow : docRows){
            SimpleDoc doc = new SimpleDoc();
            doc.setResourceKey(String.valueOf(docRow.get("ResourceKey")));
            doc.setTitle(String.valueOf(docRow.get("KeyTitle")));
            doc.setContentsGroupName(String.valueOf(docRow.get("GroupName")));
            docList.add(doc);
        }
        return docList;
    }

}
