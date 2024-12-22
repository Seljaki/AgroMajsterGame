package com.seljaki.AgroMajsterGame.http;

public class User {
    public int id;
    public String username;
    public String email;

    public User() {
    }

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
