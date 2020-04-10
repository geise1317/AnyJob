package com.example.anyjob;

public class MemberInfo {
    private String name;
    private String address;
    private String phoneNum;
    private String age;
    private String photoUrl;


    public MemberInfo(String name, String address, String phoneNum, String age, String photoUrl){
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.age = age;
        this.photoUrl = photoUrl;
    }

    public MemberInfo(String name, String address, String phoneNum, String age){
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.age = age;
    }

    public String getName(){
        return this.name;
    }
    public void setName(){
        this.name = name;
    }

    public String getAddress(){
        return this.address;
    }
    public void setAddress(){
        this.address = address;
    }

    public String getPhoneNum(){
        return this.phoneNum;
    }
    public void setPhoneNum(){
        this.phoneNum = phoneNum;
    }

    public String getAge(){
        return this.age;
    }
    public void setAge(){
        this.age = age;
    }

    public String getPhotoUrl(){
        return this.photoUrl;
    }
    public void setPhotoUrl(){
        this.photoUrl = photoUrl;
    }
}
