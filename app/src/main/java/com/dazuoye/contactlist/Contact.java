package com.dazuoye.contactlist;

public class Contact {
    private long id;
    private String name;
    private String phone;
    private boolean isPinned;    //置顶
    private String avatarUri;   //头像URI字段

    public Contact(long id, String name, String phone, boolean isPinned, String avatarUri) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.isPinned = isPinned;
        this.avatarUri = avatarUri;
    }

    // Getter方法
    public long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public boolean isPinned() { return isPinned; }
    public String getAvatarUri() { return avatarUri; }

    // Setter方法
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}