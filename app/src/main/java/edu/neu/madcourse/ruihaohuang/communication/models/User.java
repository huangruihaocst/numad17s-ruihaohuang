package edu.neu.madcourse.ruihaohuang.communication.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by huangruihao on 2017/3/6.
 */
@IgnoreExtraProperties
public class User {

    public String username;
    public String token;


    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String token){
        this.username = username;
        this.token = token;
    }
}
