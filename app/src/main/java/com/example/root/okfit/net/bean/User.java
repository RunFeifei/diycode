package com.example.root.okfit.net.bean;

import com.fei.crnetwork.dataformat.Entity;

/**
 * Created by PengFeifei on 17-7-25.
 */
public class User implements Entity {
    private int id;             // 唯一 id
    private String login;       // 登录用户名
    private String name;        // 昵称
    private String avatar_url;  // 头像链接

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return this.login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getAvatar_url() {
        return this.avatar_url;
    }

}