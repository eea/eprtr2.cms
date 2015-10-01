package eea.eprtr.cms.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import eea.eprtr.cms.model.Culture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

public class CultureServiceJdbc implements CultureService {

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Culture getByEnglishName(String name) {
        String query = "SELECT LOV_CultureID, EnglishName FROM LOV_Culture WHERE EnglishName = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        //using RowMapper anonymous class, we can create a separate RowMapper for reuse
        Culture doc = jdbcTemplate.queryForObject(query, new Object[]{name}, new RowMapper<Culture>(){

            @Override
            public Culture mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                Culture doc = new Culture(rs.getString("LOV_CultureID"), rs.getString("EnglishName"));
                return doc;
            }});

        return doc;
    }

    @Override
    public List<Culture> getAll() {
        String query = "SELECT LOV_CultureID, EnglishName FROM LOV_Culture";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Culture> docList = new ArrayList<Culture>();

        List<Map<String, Object>> docRows = jdbcTemplate.queryForList(query);

        for(Map<String, Object> docRow : docRows){
            Culture doc = new Culture(String.valueOf(docRow.get("LOV_CultureID")), String.valueOf(docRow.get("EnglishName")));
            docList.add(doc);
        }
        return docList;
    }

}
