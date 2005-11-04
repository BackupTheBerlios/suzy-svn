package de.berlios.suzy.parser;


/**
 * This abstract class contains information about items in the api, that
 * can be fully qualified. It will also store an url to the qualified object.
 *
 * @author honk
 */
public abstract class QualifiedItemInfo extends ItemInfo {
    private String lowerCaseQualifiedName;
    private String url;

    public QualifiedItemInfo(String qualifiedName, String name, String baseUrl) {
        super(name);
        this.lowerCaseQualifiedName = qualifiedName.toLowerCase();

        String qualifiedUrl = qualifiedName.replace('.', '/');
        qualifiedUrl = qualifiedUrl.substring(0, qualifiedUrl.length()-name.length()) + getNameWithCase();

        url = baseUrl+qualifiedUrl+".html";
    }

    public String getQualifiedName() {
        return lowerCaseQualifiedName;
    }

    public String getURL() {
        return url;
    }
}
