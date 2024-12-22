package com.seljaki.AgroMajsterGame.http;

public class LoginInfo {
    public String token;
    public User user;

    public LoginInfo(String token, User user) {
        this.token = token;
        this.user = user;
    }
}
