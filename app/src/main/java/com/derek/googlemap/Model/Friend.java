package com.derek.googlemap.Model;

public class Friend {

    private String fName;
    private double lati;
    private double loti;
    private String birthday;
    private String email;
    private String gender;
    private String phone;
    private String friends;
    private String imageUrl;

    public Friend(String fName, double lati, double loti, String birthday, String email, String gender, String phone, String friends, String imageUrl) {
        this.fName = fName;
        this.lati = lati;
        this.loti = loti;
        this.birthday = birthday;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
        this.friends = friends;
        this.imageUrl = imageUrl;
    }



    public String getfName() {
        return fName;
    }

    public double getLati() {
        return lati;
    }

    public double getLoti() {
        return loti;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getFriends() {
        return friends;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public void setLoti(double loti) {
        this.loti = loti;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
