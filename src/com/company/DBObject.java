package com.company;

import org.json.simple.*;

public class DBObject {
    private int id;
    private int objectType;
    private int parentObjectId;
    private String name;

    public DBObject (int id, String uid, int objectType, JSONObject data, int parentObjectId) {
        this.id = id;
        this.objectType = objectType;
        this.parentObjectId = parentObjectId;
        name = (String) data.get("name");
        if (name == null) name = uid;
    }

    public int getParentObjectId() {
        return parentObjectId;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getObjectType() {
        return objectType;
    }

    @Override
    public String toString() {
        return id + ": " + name;
    }

}
