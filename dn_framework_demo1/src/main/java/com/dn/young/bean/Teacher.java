package com.dn.young.bean;

import com.dn.young.framework.annotation.DbField;
import com.dn.young.framework.annotation.DbTable;

@DbTable("tb_teacher")
public class Teacher {

    @DbField("teacher_name")
    private String teacherName;

    @DbField("age")
    private Integer age;

    @DbField("course")
    private String course;

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "teacherName='" + teacherName + '\'' +
                ", age=" + age +
                ", course='" + course + '\'' +
                '}';
    }
}
