package com.dazuoye.contactlist;

public class Contact {
    private long id;
    private String name;
    private String phone;

    public Contact(long id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    // Getter方法
    public long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    // Setter方法
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}