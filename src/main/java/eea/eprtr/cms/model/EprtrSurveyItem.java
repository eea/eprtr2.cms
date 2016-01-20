package eea.eprtr.cms.model;

public class EprtrSurveyItem {

	private Integer surveyItemID;
    private Integer fkSurveyID;
    private String surveyItem;
    private String surveyItemResultID;
    private Integer listIndex;
//    private Timestamp updated;
	
    public EprtrSurveyItem(){
    	super();
    }
    
    public EprtrSurveyItem(Integer surveyItemID, Integer fkSurveyID, String surveyItem, String surveyItemResultID, Integer listIndex){
    	this.surveyItemID=surveyItemID;
        this.fkSurveyID=fkSurveyID;
        this.surveyItem=surveyItem;
        this.surveyItemResultID=surveyItemResultID;
        this.listIndex=listIndex;
    }
    
    
    
    public Integer getSurveyItemID() {
        return surveyItemID;
    }

    public void setSurveyItemID(Integer surveyItemID) {
        this.surveyItemID = surveyItemID;
    }

    public Integer getFkSurveyID() {
        return fkSurveyID;
    }

    public void setFkSurveyID(Integer fkSurveyID) {
        this.fkSurveyID = fkSurveyID;
    }

    public String getSurveyItem() {
        return surveyItem;
    }

    public void setSurveyItem(String surveyItem) {
        this.surveyItem = surveyItem;
    }
 
    public String getSurveyItemResultID() {
        return surveyItemResultID;
    }

    public void setSurveyItemResultID(String surveyItemResultID) {
        this.surveyItemResultID = surveyItemResultID;
    }
 
    public int getListIndex() {
        return listIndex;
    }

    public void setListIndex(Integer listIndex) {
        this.listIndex = listIndex;
    }
/*
    public Timestamp getUpdated() {
        return updated;
    }
*/
	    
}
