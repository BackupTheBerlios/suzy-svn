package de.berlios.suzy.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about a class.
 *
 * @author honk
 */
public class ClassInfo extends QualifiedItemInfo {
    private static final long serialVersionUID = 1001568155537906153L;
    private List<MethodInfo> methods = new ArrayList<MethodInfo>();
    private List<FieldInfo> fields = new ArrayList<FieldInfo>();
    private MethodInfo[] methodsArray;
    private FieldInfo[] fieldsArray;

    public ClassInfo(String name, String qualifiedName, String baseUrl) {
        super(qualifiedName, name, baseUrl);
    }

    public void addMethod(MethodInfo m) {
        if (methods == null) {
            throw new IllegalStateException("cannot add after update");
        }
        methods.add(m);
    }

    public void addField(FieldInfo f) {
        if (methods == null) {
            throw new IllegalStateException("cannot add after update");
        }
        fields.add(f);
    }

    public MethodInfo[] getMetods() {
        if (methodsArray == null) {
            throw new IllegalStateException("cannot get before update");
        }
        return methodsArray;
    }

    public FieldInfo[] getFields() {
        if (fieldsArray == null) {
            throw new IllegalStateException("cannot get before update");
        }
        return fieldsArray;
    }

    public void update() {
        methodsArray = methods.toArray(new MethodInfo[methods.size()]);
        fieldsArray = fields.toArray(new FieldInfo[fields.size()]);
        methods = null;
        fields = null;
    }
}
