package de.berlios.suzy.parser;

import java.io.Serializable;

/**
 * This abstract class is the base class of all classes
 * holding information about objects in the api.
 *
 * @author honk
 */
public abstract class ItemInfo implements Serializable {
    protected String name;
    private String casedName;

    public ItemInfo(String name) {
        this.casedName = name;
        this.name = name.toLowerCase();
    }

    public String getNameWithCase() {
        return casedName;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
