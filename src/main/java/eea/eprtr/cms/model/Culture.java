package eea.eprtr.cms.model;

/**
 * Lookup Value.
 */
public class Culture {
	
    private String code;
    
    private String name;
    

    public Culture(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getEnglishName() {
        return name;
    }
    
}
