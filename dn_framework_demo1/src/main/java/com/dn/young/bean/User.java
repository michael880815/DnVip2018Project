package com.dn.young.bean;

import com.dn.young.framework.annotation.DbField;
import com.dn.young.framework.annotation.DbTable;

@DbTable("tb_user")
public class User {

    @DbField("name")
    private String name;

    @DbField("age")
    private Integer age;

    @DbField("mobile")
    private String mobile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}
