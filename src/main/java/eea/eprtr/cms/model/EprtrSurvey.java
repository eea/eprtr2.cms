package eea.eprtr.cms.model;

import java.util.ArrayList;
import java.util.List;

public class EprtrSurvey {
	
    private Integer surveyID;

    private String surveyText;

    private String surveyLabel;

    private Integer listIndex;
    
    public EprtrSurvey(){
    	super();
    }

    public EprtrSurvey(String surveyText, String surveyLabel,Integer listIndex){
    	this.surveyText = surveyText;
    	this.surveyLabel = surveyLabel;
    	this.listIndex = listIndex;
    }

    /** Timestamp of last change. */
    //private Timestamp updated;

    private List<EprtrSurveyItem> eprtrSurveyItems = new ArrayList<EprtrSurveyItem>();; 


    public int getSurveyID() {
        return this.surveyID;
    }

    public void setSurveyID(Integer surveyID) {
        this.surveyID = surveyID;
    }

    public String getSurveyText() {
        return this.surveyText;
    }

    public void setSurveyText(String surveyText) {
        this.surveyText = surveyText;
    }
 
    public String getSurveyLabel() {
        return this.surveyLabel;
    }

    public void setSurveyLabel(String surveyLabel) {
        this.surveyLabel = surveyLabel;
    }
 
    public int getListIndex() {
        return this.listIndex;
    }

    public void setListIndex(Integer listIndex) {
        this.listIndex = listIndex;
    }

/*    public Timestamp getUpdated() {
        return this.updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }
*/
    public List<EprtrSurveyItem> getEprtrSurveyItems(){
    	return this.eprtrSurveyItems;
    }

    public void setEprtrSurveyItems(List<EprtrSurveyItem> eprtrSurveyItems){
    	this.eprtrSurveyItems = eprtrSurveyItems;
    }

}
