package eea.eprtr.cms.dao;

import java.util.List;

import eea.eprtr.cms.model.SimpleDoc;

//CRUD operations
public interface SimpleDocService {

    /**
     * Save a document.
     */
    void save(SimpleDoc doc);

    /**
     * Read.
     */
    SimpleDoc getByResourceKey(String name);

    //Update
    //void update(SimpleDoc doc);

    //Delete
    //void deleteById(int id);

    /**
     * Get All.
     */
    List<SimpleDoc> getAll();
}
