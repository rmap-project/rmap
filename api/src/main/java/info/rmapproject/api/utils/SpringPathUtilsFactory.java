package info.rmapproject.api.utils;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class SpringPathUtilsFactory implements PathUtilsFactory {

    private String apiPath;

    private String documentationPath;

    public PathUtils getInstance() {
        PathUtils utils = new PathUtils();
        utils.setApiPath(apiPath);
        utils.setDocumentationPath(documentationPath);
        return utils;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getDocumentationPath() {
        return documentationPath;
    }

    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = documentationPath;
    }
}
