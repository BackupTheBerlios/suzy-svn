package de.berlios.suzy.parser;

/**
 * Contains information about a field.
 *
 * @author honk
 */
public class FieldInfo extends QualifiedItemInfo {
    private static final long serialVersionUID = -4438032638840392428L;
    private String qualifiedType;

    public String getQualifiedType() {
        return qualifiedType;
    }

    
    private String halfQualifiedName;
    private String url;


    public FieldInfo(ClassInfo parent, String name, String baseUrl, ClassInfo urlParent) {
        super(parent.getQualifiedName() + "." + name, name, baseUrl);
        halfQualifiedName = (parent.getName() + "." + name).toLowerCase();

        url = urlParent.getURL() + "#" + getNameWithCase();
        /*url = url.replaceAll(" ","%20");
        url = url.replaceAll("\\(","%28");
        url = url.replaceAll("\\)","%29");
        url = url.replaceAll(",","%2c");*/
    }

    public FieldInfo(ClassInfo parent, String name, String baseUrl) {
        super(parent.getQualifiedName() + "." + name, name, baseUrl);
        halfQualifiedName = (parent.getName() + "." + name).toLowerCase();

        url = parent.getURL() + "#" + getNameWithCase();
        /*url = url.replaceAll(" ","%20");*/
    }

    public String getHalfQualifiedName() {
        return halfQualifiedName;
    }


    public String getURL() {
        return url;
    }
}
