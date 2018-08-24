package com.knstech.friendsapp2;

public class Blog {

    String title, desc, user_id, image, thumb_image, username;
    Object timestamp;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Blog() {

    }

    public Blog(String title, String desc, String user_id, String image, String thumb_image, String username, Object timestamp) {

        this.title = title;
        this.desc = desc;
        this.user_id = user_id;
        this.image = image;
        this.thumb_image = thumb_image;
        this.username = username;
        this.timestamp = timestamp;
    }
}

