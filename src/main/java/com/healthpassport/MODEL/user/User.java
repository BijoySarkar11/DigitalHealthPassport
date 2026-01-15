package com.healthpassport.MODEL.user;

abstract class User {
    String Name;
    int ID;

    public User(){}
    public User(String n,int i){
        Name = n;
        ID = i;
    }
}
