/**
 * This file is a model(Container) of user information
 * which store all the information of a user
 */
package com.derek.googlemap.Model;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;

public class User {
    private String name;        // name of user
    private String imageURL;    // iuser con ulr
    private String key;         // the unique key of user
    private String lati;        // latitude coordinate
    private String loti;        // longtitude coordinate
    private String email;       // user email
    private String password;    // user password
    private String phone;       // user phone number
    private int position;       // user position

    public User() {
        //empty constructor needed
    }

    public User(int position) {
        this.position = position;
    }

    /**
     * Create a user with name, icon, latitude, and longtitude
     * @param name name of user
     * @param imageUrl icon url
     * @param lati latitude
     * @param loti longtitude
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
