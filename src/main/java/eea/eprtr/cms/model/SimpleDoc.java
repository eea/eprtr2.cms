package eea.eprtr.cms.model;

/**
 * This class implements one single simple html document.
 */
public class SimpleDoc {

    private int resourceKeyID;

    /** Name of page. */
    private String resourceKey;

    /** Title of page. */
    private String title;

    private Boolean allowHTML;

    private int contentsGroupID;

    /** Content of page. */
    private String content;
    
    //resourceType    allowHTML       keyDescription  keyTitle        contentsGroupID createdDate

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getAllowHTML() {
        return allowHTML;
    }

    public void setAllowHTML(Boolean allowHTML) {
        this.allowHTML = allowHTML;
    }

    public int getContentsGroupID() {
        return contentsGroupID;
    }

    public void setContentsGroupID(int contentsGroupID) {
        this.contentsGroupID = contentsGroupID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
