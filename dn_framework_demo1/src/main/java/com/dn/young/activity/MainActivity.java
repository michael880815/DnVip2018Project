package com.dn.young.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dn.young.R;
import com.dn.young.bean.Teacher;
import com.dn.young.bean.User;
import com.dn.young.framework.database.impl.BaseDao;
import com.dn.young.framework.database.impl.DaoFactory;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BaseDao<User> userDao = DaoFactory.getInstance().getDao(BaseDao.class, User.class);

    private BaseDao<Teacher> teacherDao = DaoFactory.getInstance().getDao(BaseDao.class, Teacher.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.setName("Young");
                user.setAge(30);
                user.setMobile("18819808808");
                long result = userDao.insert(user);
                if(result > 0){
                    System.out.println("数据插入成功");
                }

                Teacher teacher = new Teacher();
                teacher.setTeacherName("Michael");
                teacher.setAge(55);
                teacher.setCourse("English");
                result = teacherDao.insert(teacher);
                if(result > 0){
                    System.out.println("数据插入成功");
                }
            }
        });

        findViewById(R.id.bt_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.setName("Michael");
                user.setAge(20);
                user.setMobile("18819808008");

                User where1 = new User();
                where1.setName("Young");
                userDao.update(user, where1);

                Teacher teacher = new Teacher();
                teacher.setTeacherName("Gerrard");
                teacher.setAge(38);
                teacher.setCourse("English");

                Teacher where2 = new Teacher();
                where2.setTeacherName("Michael");
                teacherDao.update(teacher, where2);
            }
        });

        findViewById(R.id.bt_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User where1 = new User();
                where1.setName("Young");
                userDao.delete(where1);

                Teacher where2 = new Teacher();
                where2.setTeacherName("Michael");
                teacherDao.delete(where2);
            }
        });

        findViewById(R.id.bt_query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User where1 = new User();
                where1.setName("Young");
                List<User> userResult = userDao.query(where1);
                System.out.println(userResult.toString());

                Teacher where2 = new Teacher();
                where2.setTeacherName("Michael");
                List<Teacher> teacherResult = teacherDao.query(where2);
                System.out.println(teacherResult.toString());
            }
        });
    }

}
