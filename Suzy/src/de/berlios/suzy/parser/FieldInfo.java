package de.berlios.suzy.parser;

/**
 * Contains information about a field.
 *
 * @author honk
 */
public class FieldInfo extends ItemInfo {
    private static final long serialVersionUID = -4438032638840392428L;
    private String qualifiedType;

    public FieldInfo(String name, String qualifiedType) {
        super(name);
        this.qualifiedType = qualifiedType;
    }

    public String getQualifiedType() {
        return qualifiedType;
    }


}
