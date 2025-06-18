package com.dazuoye.contactlist;

public class Contact {
    private long id;
    private String name;
    private String phone;
    private boolean isPinned;

    public Contact(long id, String name, String phone) {
        this(id, name, phone, false); // 默认不置顶
    }

    public Contact(long id, String name, String phone, boolean isPinned) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.isPinned = isPinned;
    }

    // Getter方法
    public long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public boolean isPinned() { return isPinned; }

    // Setter方法
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}