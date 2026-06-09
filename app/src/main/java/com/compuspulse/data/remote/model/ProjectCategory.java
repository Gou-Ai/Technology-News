package com.compuspulse.data.remote.model;

public class ProjectCategory {
    private int id;
    private String name;

    public ProjectCategory() {
    }

    public ProjectCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name == null ? "" : name;
    }
}
