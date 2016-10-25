package com.jufan.cyss.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.avos.avoscloud.AVUser;

/**
 * Created by cyjss on 2015/3/2.
 */
@Table(name = "Simple")
public class Simple extends Model {
    @Column(name = "key", unique = true)
    public String key;
    @Column(name = "value")
    public String value;

    public Simple() {
    }

    public Simple(String key, String value) {
        key = getUsername() + "_" + key;
        this.key = key;
        this.value = value;
    }

    public static Simple getByKey(String key) {
        String _key = getUsername() + "_" + key;
        Simple s = new Select().from(Simple.class).where("key=?", _key).executeSingle();
        if (s == null) {
            s = new Simple(key, "");
        }
        return s;
    }

    public static void deleteByKy(String key) {
        key = getUsername() + "_" + key;
        Simple s = new Select().from(Simple.class).where("key=?", key).executeSingle();
        s.delete();
    }

    private static String getUsername() {
        String username = "anon";
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            username = user.getMobilePhoneNumber();
        }
        return username;
    }
}
