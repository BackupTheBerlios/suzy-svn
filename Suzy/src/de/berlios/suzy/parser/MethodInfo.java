package de.berlios.suzy.parser;


/**
 * Contains information about methods
 *
 * @author honk
 */
public class MethodInfo extends QualifiedItemInfo {
    private static final long serialVersionUID = 2127101233130113491L;
    private String halfQualifiedName;
    private String url;



    public MethodInfo(ClassInfo parent, String name, String signature, String baseUrl, ClassInfo urlParent) {
        super(parent.getQualifiedName() + "." + name, name, baseUrl);
        halfQualifiedName = (parent.getName() + "." + name).toLowerCase();

        url = urlParent.getURL() + "#" + getNameWithCase() + signature;
        url = url.replaceAll(" ","%20");
        url = url.replaceAll("\\(","%28");
        url = url.replaceAll("\\)","%29");
        url = url.replaceAll(",","%2c");
    }

    public MethodInfo(ClassInfo parent, String name, String signature, String baseUrl) {
        super(parent.getQualifiedName() + "." + name, name, baseUrl);
        halfQualifiedName = (parent.getName() + "." + name).toLowerCase();

        url = parent.getURL() + "#" + getNameWithCase() + signature;
        url = url.replaceAll(" ","%20");
    }

    public String getHalfQualifiedName() {
        return halfQualifiedName;
    }


    public String getURL() {
        return url;
    }
}
