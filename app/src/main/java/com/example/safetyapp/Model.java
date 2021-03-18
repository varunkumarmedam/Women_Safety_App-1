package com.example.safetyapp;

public class Model {
    String name, phonenumber, selected_type;

    public Model() {
    }

    public Model(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Model(String name, String phonenumber) {
        this.name = name;
        this.phonenumber = phonenumber;
    }

    public Model(String name, String phonenumber, String selected_type) {
        this.name = name;
        this.phonenumber = phonenumber;
        this.selected_type = selected_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSelected_type() {
        return selected_type;
    }

    public void setSelected_type(String selected_type) {
        this.selected_type = selected_type;
    }
}
