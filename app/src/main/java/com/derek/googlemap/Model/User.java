/**
 * This is a data container for user
 */

package com.derek.googlemap.Model;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;

public class User {
    private String name;
    private String imageURL;
    private String key;
    private String lati;
    private String loti;
    private String email;
    private String password;
    private String birthday;
    private String friends;
    private String phone;
    private int position;

    public User() {
        //empty constructor needed for firebase toObject() method
    }

    /**
     * Initialize a user with Position
     * @param position
     */
    public User(int position) {
        this.position = position;
    }

    /**
     * Initialize a user with several information
     *
     * @param name  user name
     * @param imageUrl  image path
     * @param lati latitude
     * @param loti  longtitude
     */
    public User(String name, String imageUrl, String lati, String loti) {
        if (TextUtils.isEmpty(name)) {
            name = "No Name";
        }
        this.name = name;
        this.imageURL = imageUrl;
        this.lati = lati;
        this.loti = loti;
    }

    /******** Setters and Getters ********/

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLati() {
        return lati;
    }

    public void setLati(String lati) {
        this.lati = lati;
    }

    public String getLoti() {
        return loti;
    }

    public void setLoti(String loti) {
        this.loti = loti;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
