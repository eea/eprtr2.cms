package eea.eprtr.cms.dao;

import java.util.List;

import eea.eprtr.cms.model.EprtrSurvey;
import eea.eprtr.cms.model.EprtrSurveyItem;

public interface EprtrSurveyService {
    /**
     * Save a survey.
     */
    void save(EprtrSurvey surv);

    /**
     * Add a  new survey.
     */
	void add(Integer itemIndex);

    /**
     * Delete a survey.
     */
	void delete(Integer surveyId);


	void saveItem(EprtrSurveyItem survItem);

    /**
     * Read.
     */
    EprtrSurvey getBySurveyID(int surveyID);

    List<EprtrSurveyItem> getItemsBySurveyID(int surveyID);
    //Delete
    //void deleteById(int id);

    /**
     * Get All surveys.
     */
    List<EprtrSurvey> getAll();

	EprtrSurveyItem newItem(Integer surveyID, Integer itemIndex);
	
    void deleteItem(Integer surveyItemID);


}
